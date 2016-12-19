package com.coreoz.ppt;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

/**
 * Bind PowerPoint variables to actions :<br/>
 * - text replacement,<br/>
 * - image replacement,<br/>
 * - text and shape styling,<br/>
 * - text, shape and image hiding.<br/>
 */
public class PptMapper {

	private final Map<String, PptTextMapper> textMapping;
	private final Map<String, PptImageMapper> imageMapping;
	private final Map<String, PptHidingMapper> hideMapping;
	private final Map<String, PptStyleTextMapper> styleTextMapping;

	public PptMapper() {
		this.textMapping = new HashMap<>();
		this.imageMapping = new HashMap<>();
		this.hideMapping = new HashMap<>();
		this.styleTextMapping = new HashMap<>();
	}

	// configuration

	/**
	 * Replace a text variable with a value.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName/</code> in the PPT presentation
	 * @param value The value that will replaced the variable.
	 * {@link Object#toString()}} will be called upon the object.
	 * If the Object is null, then the variable will be replaced by an empty String
	 * @return The mapper instance
	 */
	public PptMapper text(String variableName, Object value) {
		textMapping.put(variableName, PptTextMapper.of(value, null));
		return this;
	}

	/**
	 * Replace an image placeholder by an other image.
	 * This image placeholder is identified with a link placed on it.
	 * To fit the placeholder, the replacement image will be resized and truncated
	 * relatively to its center.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param imageData The raw data of the image that will replace the placeholder
	 * @return The mapper instance
	 */
	public PptMapper image(String variableName, byte[] imageData) {
		return image(variableName, imageData, PptImageReplacementMode.RESIZE_CROP);
	}

	/**
	 * Replace an image placeholder by an other image.
	 * This image placeholder is identified with a link placed on it.
	 * To fit the placeholder, the replacement mode will be used to
	 * resize the image.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param imageData The raw data of the image that will replace the placeholder
	 * @param replacementMode Define how the image should be resized, see {@link PptImageReplacementMode}
	 * @return The mapper instance
	 */
	public PptMapper image(String variableName, byte[] imageData,
			PptImageReplacementMode replacementMode) {
		PictureType imageFormat = ImagesUtils.guessPictureType(imageData);
		if(imageFormat == null) {
			throw new IllegalArgumentException(
				"Enable to determine the image type, "
				+ "you may want to directly specify the image type using: "
				+ "image(String variableName, byte[] imageData, "
				+ "PptImageReplacementMode replacementMode, PictureType imageFormat)"
			);
		}

		return image(variableName, imageData, replacementMode, imageFormat);
	}

	/**
	 * Replace an image placeholder by an other image.
	 * This image placeholder is identified with a link placed on it.
	 * To fit the placeholder, the replacement mode will be used to
	 * resize the image.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param imageData The raw data of the image that will replace the placeholder
	 * @param replacementMode Define how the image should be resized, see {@link PptImageReplacementMode}
	 * @param imageFormat specify the picture format that will be used in the PPT ;
	 * note that this format may differ from the original format of the replacement image
	 * @return The mapper instance
	 */
	public PptMapper image(String variableName, byte[] imageData,
			PptImageReplacementMode replacementMode, PictureType imageFormat) {
		imageMapping.put(variableName, PptImageMapper.of(imageFormat, replacementMode, imageData));
		return this;
	}

	/**
	 * Remove from the presentation a text, a shape or an image
	 * based on a variable placed in the link of the element.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @return The mapper instance
	 */
	public PptMapper hide(String variableName) {
		hideMapping.put(variableName, PptHidingMapper.of(arg -> true));
		return this;
	}

	/**
	 * Remove from the presentation a text, a shape or an image
	 * based on a variable placed in the link of the element.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param shouldHide The predicate that accepts the variable argument
	 * @return The mapper instance
	 */
	public PptMapper hide(String variableName, Predicate<String> shouldHide) {
		hideMapping.put(variableName, PptHidingMapper.of(shouldHide));
		return this;
	}

	/**
	 * Style a text in the presentation by directly modifying the {@link TextRun}
	 * object provided by POI.
	 * The text object is identified with a link placed on it.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param applyText The consumer that will directly change the {@link TextRun}
	 * @return The mapper instance
	 */
	public PptMapper styleText(String variableName, Consumer<TextRun> applyText) {
		styleTextMapping.put(variableName, PptStyleTextMapper.of((arg, textRun) -> applyText.accept(textRun)));
		return this;
	}

	/**
	 * Style a text in the presentation by directly modifying the {@link TextRun}
	 * object provided by POI.
	 * The text object is identified with a link placed on it.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:'argument'/</code> in the PPT presentation
	 * @param applyText The bi consumer that will directly change the {@link TextRun},
	 * the first consumer parameter is the variable argument
	 * @return The mapper instance
	 */
	public PptMapper styleText(String variableName, BiConsumer<String, TextRun> applyText) {
		styleTextMapping.put(variableName, PptStyleTextMapper.of(applyText));
		return this;
	}

	// helper

	/**
	 * Fill in the template with the mapper data.
	 * @see PptTemplates#process(InputStream, PptMapper)
	 */
	public XMLSlideShow processTemplate(InputStream templateData) {
		return PptTemplates.process(templateData, this);
	}

	/**
	 * Fill in the template with the mapper data.
	 * The template passed as a parameter will directly be modified.
	 * @see PptTemplates#process(InputStream, PptMapper)
	 * @return The template passed as a parameter
	 */
	public XMLSlideShow processTemplate(XMLSlideShow templateData) {
		return PptTemplates.processPpt(templateData, this);
	}

	// package API

	Optional<String> textMapping(String variableName) {
		return Optional
			.ofNullable(textMapping.get(variableName))
			.map(mapping -> mapping.getValue() == null ?
				nullToEmpty(mapping.getSupplierToValue().get()).toString()
				: nullToEmpty(mapping.getValue()).toString()
			);
	}

	Optional<PptImageMapper> imageMapping(String variableName) {
		return Optional.ofNullable(imageMapping.get(variableName));
	}

	Optional<Boolean> hideMapping(String variableName, String argument) {
		return Optional
			.ofNullable(hideMapping.get(variableName))
			.map(hidingMapper -> hidingMapper.getShouldHide().test(argument));
	}

	Optional<BiConsumer<String, TextRun>> styleText(String variableName) {
		return Optional
			.ofNullable(styleTextMapping.get(variableName))
			.map(PptStyleTextMapper::getApplyText);
	}

	// internal

	private static Object nullToEmpty(Object value) {
		return value == null ? "" : value;
	}

}

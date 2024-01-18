package com.coreoz.ppt;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;

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
	private final Map<String, PptStyleShapeMapper> styleShapeMapping;

	private char variableCharDelimiter;
	private char variableCharStart;

	public PptMapper() {
		this.variableCharDelimiter = '/';
		this.variableCharStart = '$';

		this.textMapping = new HashMap<>();
		this.imageMapping = new HashMap<>();
		this.hideMapping = new HashMap<>();
		this.styleTextMapping = new HashMap<>();
		this.styleShapeMapping = new HashMap<>();
	}

	public PptMapper setVariableCharDelimiter(char variableCharDelimiter) {
		this.variableCharDelimiter = variableCharDelimiter;
		return this;
	}

	public PptMapper setVariableCharStart(char variableCharStart) {
		this.variableCharStart = variableCharStart;
		return this;
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
	 * Replace a text variable with a value.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param toValue The function that will return the value that will replace the variable.
	 * The function take the variable argument as a parameter.
	 * {@link Object#toString()}} will be called upon the object returned by the function.
	 * If the Object is null, then the variable will be replaced by an empty String
	 * @return The mapper instance
	 */
	public PptMapper text(String variableName, Function<String, Object> toValue) {
		textMapping.put(variableName, PptTextMapper.of(null, toValue));
		return this;
	}

	/**
	 * Replace an image placeholder by an other image.
	 * This image placeholder is identified with a link placed on it.
	 * To fit the placeholder, the replacement image will be resized and truncated
	 * relatively to its center.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
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
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
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
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param imageData The raw data of the image that will replace the placeholder
	 * @param replacementMode Define how the image should be resized, see {@link PptImageReplacementMode}
	 * @param imageFormat specify the picture format that will be used in the PPT ;
	 * note that this format may differ from the original format of the replacement image
	 * @return The mapper instance
	 */
	public PptMapper image(String variableName, byte[] imageData,
			PptImageReplacementMode replacementMode, PictureType imageFormat) {
		return image(
			variableName,
			imageData,
			replacementMode,
			imageFormat,
			PptImageMapper.DEFAULT_QUALITY_FACTOR,
			PptImageMapper.DEFAULT_QUALITY_MULTIPLICATOR
		);
	}

	/**
	 * Replace an image placeholder by an other image.
	 * This image placeholder is identified with a link placed on it.
	 * To fit the placeholder, the replacement mode will be used to
	 * resize the image.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param imageData The raw data of the image that will replace the placeholder
	 * @param replacementMode Define how the image should be resized, see {@link PptImageReplacementMode}
	 * @param imageFormat specify the picture format that will be used in the PPT ;
	 * note that this format may differ from the original format of the replacement image
	 * @param qualityFactor The target picture quality between 0 (low quality) and 1 (high quality)
	 * @param qualityMultiplicator Another picture quality parameter between 0 (the image will not be shown),
	 * 1 (the image will be sized to fit exactly its placeholder), N (the image will be resized to fit its placeholder size times N)
	 * @return The mapper instance
	 */
	public PptMapper image(String variableName, byte[] imageData,
			PptImageReplacementMode replacementMode, PictureType imageFormat,
			float qualityFactor,
			double qualityMultiplicator) {
		imageMapping.put(
			variableName,
			PptImageMapper.of(imageFormat, replacementMode, imageData, qualityFactor, qualityMultiplicator)
		);
		return this;
	}

	/**
	 * Remove from the presentation a text, a shape or an image
	 * based on a variable placed in the link of the element.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @return The mapper instance
	 */
	public PptMapper hide(String variableName) {
		return hide(variableName, arg -> true);
	}

	/**
	 * Remove from the presentation a text, a shape or an image
	 * based on a variable placed in the link of the element.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param shouldHide The predicate that accepts the variable argument ;
	 * the predicate must return true to hide the element, if false is returned
	 * the element remain is place and the link upon it is removed
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
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param applyText The consumer that will directly change the {@link TextRun}
	 * @return The mapper instance
	 */
	public PptMapper styleText(String variableName, Consumer<TextRun> applyText) {
		return styleText(variableName, (arg, textRun) -> applyText.accept(textRun));
	}

	/**
	 * Style a text in the presentation by directly modifying the {@link TextRun}
	 * object provided by POI.
	 * The text object is identified with a link placed on it.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param applyText The bi consumer that will directly change the {@link TextRun},
	 * the first consumer parameter is the variable argument
	 * @return The mapper instance
	 */
	public PptMapper styleText(String variableName, BiConsumer<String, TextRun> applyText) {
		styleTextMapping.put(variableName, PptStyleTextMapper.of(applyText));
		return this;
	}

	/**
	 * Style a shape in the presentation by directly modifying the {@link XSLFSimpleShape}
	 * object provided by POI.
	 * The shape object is identified with a link placed on it.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param applyShape The consumer that will directly change the {@link XSLFSimpleShape}
	 * @return The mapper instance
	 */
	public PptMapper styleShape(String variableName, Consumer<XSLFSimpleShape> applyShape) {
		return styleShape(variableName, (arg, shape) -> applyShape.accept(shape));
	}

	/**
	 * Style a shape in the presentation by directly modifying the {@link XSLFSimpleShape}
	 * object provided by POI.
	 * The shape object is identified with a link placed on it.
	 *
	 * @param variableName The variable name.
	 * It should be in the form of <code>$/variableName:argument/</code> in the PPT presentation
	 * @param applyShape The bi consumer that will directly change the {@link XSLFSimpleShape},
	 * the first consumer parameter is the variable argument
	 * @return The mapper instance
	 */
	public PptMapper styleShape(String variableName, BiConsumer<String, XSLFSimpleShape> applyShape) {
		styleShapeMapping.put(variableName, PptStyleShapeMapper.of(applyShape));
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

	Optional<String> textMapping(String variableName, String argument) {
		return Optional
			.ofNullable(textMapping.get(variableName))
			.map(mapping -> mapping.getValue() == null ?
				nullToEmpty(mapping.getArgumentToValue().apply(argument)).toString()
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
			.map(PptStyleTextMapper::getApplyStyle);
	}

	Optional<BiConsumer<String, XSLFSimpleShape>> styleShape(String variableName) {
		return Optional
			.ofNullable(styleShapeMapping.get(variableName))
			.map(PptStyleShapeMapper::getApplyStyle);
	}

	// internal

	private static Object nullToEmpty(Object value) {
		return value == null ? "" : value;
	}

}

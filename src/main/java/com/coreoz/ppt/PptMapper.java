package com.coreoz.ppt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.TextRun;

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
	
	public PptMapper text(String variableName, Object value) {
		textMapping.put(variableName, PptTextMapper.of(value, null));
		return this;
	}
	
	public PptMapper imageJpg(String variableName, byte[] imageData) {
		imageMapping.put(variableName, PptImageMapper.of(PictureType.JPEG, imageData));
		return this;
	}
	
	public PptMapper hide(String variableName) {
		hideMapping.put(variableName, PptHidingMapper.of(arg -> true));
		return this;
	}
	
	public PptMapper hide(String variableName, Predicate<String> shouldHide) {
		hideMapping.put(variableName, PptHidingMapper.of(shouldHide));
		return this;
	}
	
	public PptMapper styleText(String variableName, Consumer<TextRun> applyText) {
		styleTextMapping.put(variableName, PptStyleTextMapper.of((arg, textRun) -> applyText.accept(textRun)));
		return this;
	}
	
	public PptMapper styleText(String variableName, BiConsumer<String, TextRun> applyText) {
		styleTextMapping.put(variableName, PptStyleTextMapper.of(applyText));
		return this;
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

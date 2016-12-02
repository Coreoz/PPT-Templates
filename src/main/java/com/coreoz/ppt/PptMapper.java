package com.coreoz.ppt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.poi.sl.usermodel.PictureData.PictureType;

public class PptMapper {
	
	private final Map<String, PptTextMapper> textMapping;
	private final Map<String, PptImageMapper> imageMapping;
	private final Map<String, PptHidingMapper> hideMapping;

	public PptMapper() {
		this.textMapping = new HashMap<>();
		this.imageMapping = new HashMap<>();
		this.hideMapping = new HashMap<>();
	}
	
	public PptMapper text(String variableName, Object value) {
		textMapping.put(variableName, PptTextMapper.of(value, null, null, null));
		return this;
	}
	
	public PptMapper imageJpg(String variableName, byte[] imageData) {
		imageMapping.put(variableName, PptImageMapper.of(PictureType.JPEG, imageData));
		return this;
	}
	
	public PptMapper hide(String variableName, Predicate<String> shouldHide) {
		hideMapping.put(variableName, PptHidingMapper.of(shouldHide));
		return this;
	}
	
	// package API
	
	Optional<PptTextMapper> textMapping(String variableName) {
		return Optional.ofNullable(textMapping.get(variableName));
	}

	Optional<PptImageMapper> imageMapping(String variableName) {
		return Optional.ofNullable(imageMapping.get(variableName));
	}

	Optional<Boolean> hideMapping(String variableName, String argument) {
		return Optional
			.ofNullable(hideMapping.get(variableName))
			.map(hidingMapper -> hidingMapper.getShouldHide().test(argument));
	}

}

package com.coreoz.ppt;

import org.apache.poi.sl.usermodel.PictureData.PictureType;

import lombok.Value;

@Value(staticConstructor = "of")
class PptImageMapper {

	private final PictureType targetFormat;
	private final PptImageReplacementMode replacementMode;
	private final byte[] value;

}

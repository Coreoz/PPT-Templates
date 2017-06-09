package com.coreoz.ppt;

import org.apache.poi.sl.usermodel.PictureData.PictureType;

import lombok.Value;

@Value(staticConstructor = "of")
class PptImageMapper {

	public static final float DEFAULT_QUALITY_FACTOR = 1F;
	public static final double DEFAULT_QUALITY_MULTIPLICATOR = 2.0;

	private final PictureType targetFormat;
	private final PptImageReplacementMode replacementMode;
	private final byte[] value;
	private final float qualityFactory;
	private final double qualityMultiplicator;

}

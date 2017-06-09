package com.coreoz.ppt;

import lombok.AllArgsConstructor;

/**
 * Define how a new image should be resized to replace an existing image in a PPT.
 * Resized images will always be placed in the top left corner
 * of the original image placeholder frame.
 */
@AllArgsConstructor
public enum PptImageReplacementMode {
	/**
	 * The new image will be resized the best fit the existing image frame,
	 * then the new image will be cropped from its center to fit exactly the original image frame.
	 */
	RESIZE_CROP(ImagesUtils::resizeCrop),
	/**
	 * The new image will be resized the best fit the existing image frame,
	 * but no cropping will be applied: that means that the new image
	 * will very likely overstep the original image frame.
	 */
	RESIZE_ONLY(ImagesUtils::resizeOnly),
	;

	private final ResizeFunction resizeFunction;

	byte[] resize(byte[] imageData, String targetFormat, int width, int height,
			float qualityFactor, double qualityMultiplicator) {
		return resizeFunction.resizeImage(imageData, targetFormat, width, height, qualityFactor, qualityMultiplicator);
	}

	@FunctionalInterface
	private static interface ResizeFunction {
		byte[] resizeImage(
			byte[] imageData,
			String targetFormat,
			int width,
			int height,
			float qualityFactor,
			double qualityMultiplicator
		);
	}

}

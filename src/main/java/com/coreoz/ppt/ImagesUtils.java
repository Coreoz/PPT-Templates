package com.coreoz.ppt;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;

class ImagesUtils {

	private static final Logger logger = LoggerFactory.getLogger(ImagesUtils.class);

	private static final int QUALITY_MULTIPLICATOR = 2;

	// resizing

	static byte[] resizeCrop(byte[] imageData, String targetFormat, int width, int height) {
		return resize(imageData, targetFormat, width, height, true);
	}

	static byte[] resizeOnly(byte[] imageData, String targetFormat, int width, int height) {
		return resize(imageData, targetFormat, width, height, false);
	}

	@SneakyThrows
	private static byte[] resize(byte[] imageData, String targetFormat, int width, int height, boolean crop) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Builder<? extends InputStream> builder = Thumbnails
			.of(new ByteArrayInputStream(imageData))
			.outputQuality(1F)
			.size(width * QUALITY_MULTIPLICATOR, height * QUALITY_MULTIPLICATOR);

		if(crop) {
			builder.crop(Positions.CENTER);
		}

		try {
			builder
				.outputFormat(targetFormat)
				.toOutputStream(byteArrayOutputStream);
		} catch (IOException e) {
			logger.error("Cannot resize image to format {}", targetFormat, e);
			return null;
		}

		return byteArrayOutputStream.toByteArray();
	}

	// image size

	@SneakyThrows
	static Dimension imageDimension(byte[] pictureData) {
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(pictureData));
		return new Dimension(
			bufferedImage.getWidth() / QUALITY_MULTIPLICATOR,
			bufferedImage.getHeight() / QUALITY_MULTIPLICATOR
		);
	}

	// image mime type

	static PictureType guessPictureType(byte[] pictureData) {
		for(ImageType imageType : ImageType.values()) {
			if(startsWith(pictureData, imageType.startPattern)) {
				return imageType.poiType;
			}
		}

		return null;
	}

	@AllArgsConstructor
	private enum ImageType {
		PNG(PictureType.PNG, new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A }),
		GIF(PictureType.GIF, new byte[] { (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38 }),
		JPEG(PictureType.JPEG, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }),
		BMP(PictureType.BMP, new byte[] { (byte) 0x42, (byte) 0x4D }),
		;

		private final PictureType poiType;
		private final byte[] startPattern;
	}

	private static boolean startsWith(byte[] source, byte[] match) {
		if(match.length > source.length) {
			return false;
		}

		for(int i=0; i<match.length; i++) {
			if(source[i] != match[i]) {
				return false;
			}
		}

		return true;
	}

}

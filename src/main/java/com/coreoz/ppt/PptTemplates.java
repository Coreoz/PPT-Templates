package com.coreoz.ppt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;

import lombok.SneakyThrows;
import lombok.Value;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * Enable to update PowerPoint presentation with dynamic data via a variable system.<br/>
 * <br/>
 * Variable format in the PPT is always: <code>$/variableName:'argument'/</code><br/>
 * <br/>
 * <strong>Be aware that the template presentation should NOT
 * include any SmartArt or any Excel graphic.
 * This would very likely lead to a corrupted presentation.</strong>
 */
public class PptTemplates {

	/**
	 * Fill in the template with the mapper data.
	 * @param templateData The stream to the template data
	 * @param mapper The object used to fill in the template
	 * @return A PowerPoint presentation filled with data
	 */
	@SneakyThrows
	public static XMLSlideShow process(InputStream templateData, PptMapper mapper) {
		XMLSlideShow ppt = new XMLSlideShow(templateData);

		processPpt(ppt, mapper);

		return ppt;
	}

	/**
	 * Fill in the template with the mapper data.
	 * The template passed as a parameter will directly be modified.
	 * @param ppt The template presentation to be filled
	 * @param mapper The object used to fill in the template
	 * @return The template passed as a parameter
	 */
	public static XMLSlideShow processPpt(XMLSlideShow ppt, PptMapper mapper) {
		for(XSLFSlide slide : ppt.getSlides()) {
			List<ImageToReplace> imagesToReplace = new ArrayList<>();
			List<XSLFShape> shapesToDelete = new ArrayList<>();

			for(XSLFShape shape : slide.getShapes()) {
				if(processShape(imagesToReplace, shape, mapper)) {
					shapesToDelete.add(shape);
				}
			}

			for(XSLFShape shapeToDelete : shapesToDelete) {
				slide.removeShape(shapeToDelete);
			}

			for(ImageToReplace imageToReplace : imagesToReplace) {
				replaceImage(ppt, slide, imageToReplace);
			}
		}

		return ppt;
	}

	// internal

	/**
	 * Handles shape modification
	 * @return true is the shape should be removed
	 */
	private static boolean processShape(List<ImageToReplace> imagesToReplace, XSLFShape shape, PptMapper mapper) {
		if(shape instanceof XSLFTextShape) {
			return processTextShape((XSLFTextShape) shape, mapper);
		}
		if(shape instanceof XSLFTable) {
			return processTableShape((XSLFTable) shape, mapper);
		}
		if(shape instanceof XSLFPictureShape) {
			return processImageShape(imagesToReplace, (XSLFPictureShape) shape, mapper);
		}
		return false;
	}

	private static boolean processImageShape(List<ImageToReplace> imagesToReplace, XSLFPictureShape imageShape, PptMapper mapper) {
		Optional<PptVariable> imageVariable = parseHyperlinkVariable(imageShape);
		if(shouldHide(imageVariable, mapper)) {
			return true;
		}

		imageVariable
			.flatMap(variable -> mapper.imageMapping(variable.getName()))
			.ifPresent(imageMapper -> {
				imagesToReplace.add(ImageToReplace.of(imageShape, imageMapper));
			});

		return false;
	}

	private static void replaceImage(XMLSlideShow ppt, XSLFSlide slide, ImageToReplace imageToReplace) {
		byte[] newPictureResized = resizeImage(
			imageToReplace.imageMapper.getValue(),
			imageToReplace.imageMapper.getTargetFormat().name(),
			(int) imageToReplace.toReplace.getAnchor().getWidth(),
			(int) imageToReplace.toReplace.getAnchor().getHeight()
		);
		XSLFPictureData newPictureData = ppt.addPicture(newPictureResized, imageToReplace.imageMapper.getTargetFormat());
		XSLFPictureShape newPictureShape = slide.createPicture(newPictureData);
		newPictureShape.setAnchor(imageToReplace.toReplace.getAnchor());
		slide.removeShape(imageToReplace.toReplace);
	}

	@SneakyThrows
	private static byte[] resizeImage(byte[] imageData, String targetFormat, int width, int height) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Thumbnails
			.of(new ByteArrayInputStream(imageData))
			.crop(Positions.CENTER)
			.outputQuality(1F)
			.size(width, height)
			.outputFormat(targetFormat)
			.toOutputStream(byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	private static boolean processTableShape(XSLFTable tableShape, PptMapper mapper) {
		for(XSLFTableRow row : tableShape.getRows()) {
			for(XSLFTableCell cell : row.getCells()) {
				processTextParagraphs(cell.getTextParagraphs(), mapper);
			}
		}

		return false;
	}

	private static boolean processTextShape(XSLFTextShape textShape, PptMapper mapper) {
		if(shouldHide(textShape, mapper)) {
			return true;
		}

		processTextParagraphs(textShape.getTextParagraphs(), mapper);

		return false;
	}

	private static void processTextParagraphs(List<XSLFTextParagraph> paragraphs, PptMapper mapper) {
		for (XSLFTextParagraph paragraph : paragraphs) {
			for (XSLFTextRun textRun : paragraph.getTextRuns()) {
				Optional<PptVariable> parsedHyperlinkVariale = parseHyperlinkVariale(textRun.getHyperlink());

				parsedHyperlinkVariale
					.flatMap(variable -> mapper.styleText(variable.getName()))
					.ifPresent(styler -> styler.accept(parsedHyperlinkVariale.get().getArg1(), textRun));

				if(shouldHide(parsedHyperlinkVariale, mapper)) {
					textRun.setText("");
				} else if(parsedHyperlinkVariale.isPresent()) {
					textRun.getXmlObject().getRPr().unsetHlinkClick();
				}
			}

			PptParser.replaceTextVariable(paragraph, mapper);
		}
	}

	private static boolean shouldHide(XSLFSimpleShape simpleShape, PptMapper mapper) {
		Optional<PptVariable> parsedHyperlinkVariale = parseHyperlinkVariable(simpleShape);

		// if the link is a variable, remove the link
		parsedHyperlinkVariale.ifPresent(variable -> {
			String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr";
			XmlObject[] rs = simpleShape.getXmlObject().selectPath(xquery);
			CTNonVisualDrawingProps nvPr = (CTNonVisualDrawingProps) rs[0];
			nvPr.unsetHlinkClick();
		});

		return shouldHide(parsedHyperlinkVariale, mapper);
	}

	private static boolean shouldHide(Optional<PptVariable> variable, PptMapper mapper) {
		return variable
			.flatMap(shapeVariable ->
				mapper.hideMapping(shapeVariable.getName(), shapeVariable.getArg1())
			)
			.orElse(false);
	}

	private static Optional<PptVariable> parseHyperlinkVariable(SimpleShape<?, ?> simpleShape) {
		return parseHyperlinkVariale(simpleShape.getHyperlink());
	}


	private static Optional<PptVariable> parseHyperlinkVariale(Hyperlink<?, ?> link) {
		if(link != null && link.getTypeEnum() == HyperlinkType.URL) {
			return PptParser.parse(link.getAddress());
		}
		return Optional.empty();
	}

	@Value(staticConstructor = "of")
	private static class ImageToReplace {
		private final XSLFPictureShape toReplace;
		private final PptImageMapper imageMapper;
	}

}

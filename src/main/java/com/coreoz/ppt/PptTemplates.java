package com.coreoz.ppt;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLDocumentPart.RelationPart;
import org.apache.poi.PptPoiBridge;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
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
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import lombok.SneakyThrows;
import lombok.Value;

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
			processShapesContainer(slide, ppt, mapper);
		}

		return ppt;
	}

	// internal

	private static void processShapesContainer(ShapeContainer<XSLFShape, ?> shapeContainer, XMLSlideShow ppt, PptMapper mapper) {
		List<ImageToReplace> imagesToReplace = new ArrayList<>();
		List<XSLFShape> shapesToDelete = new ArrayList<>();

		for(XSLFShape shape : shapeContainer.getShapes()) {
			if(processShape(shape, imagesToReplace, ppt, mapper)) {
				shapesToDelete.add(shape);
			}
		}

		for(XSLFShape shapeToDelete : shapesToDelete) {
			shapeContainer.removeShape(shapeToDelete);
		}

		for(ImageToReplace imageToReplace : imagesToReplace) {
			replaceImage(ppt, shapeContainer, imageToReplace);
		}
	}

	/**
	 * Handles shape modification
	 * @return true is the shape should be removed
	 */
	private static boolean processShape(XSLFShape shape, List<ImageToReplace> imagesToReplace, XMLSlideShow ppt, PptMapper mapper) {
		if(shape instanceof XSLFTextShape) {
			return processTextShape((XSLFTextShape) shape, mapper);
		}
		if(shape instanceof XSLFTable) {
			return processTableShape((XSLFTable) shape, mapper);
		}
		if(shape instanceof XSLFPictureShape) {
			return processImageShape((XSLFPictureShape) shape, imagesToReplace, mapper);
		}
		if(shape instanceof XSLFGroupShape) {
			return processGroupShape((XSLFGroupShape) shape, ppt, mapper);
		}

		return false;
	}

	private static boolean processGroupShape(XSLFGroupShape groupShape, XMLSlideShow ppt, PptMapper mapper) {
		processShapesContainer(groupShape, ppt, mapper);

		return false;
	}

	private static boolean processImageShape(XSLFPictureShape imageShape, List<ImageToReplace> imagesToReplace, PptMapper mapper) {
		Optional<PptVariable> imageVariable = parseHyperlinkVariable(imageShape);
		if(shouldHide(imageVariable, mapper)) {
			return true;
		}

		imageVariable
			.flatMap(variable -> mapper.imageMapping(variable.getName()))
			.ifPresent(imageMapper ->
				imagesToReplace.add(ImageToReplace.of(imageShape, imageMapper))
			);

		styleShape(imageShape, imageVariable, mapper);

		return false;
	}

	private static void replaceImage(XMLSlideShow ppt, ShapeContainer<XSLFShape, ?> shapeContainer, ImageToReplace imageToReplace) {
		byte[] newPictureResized = imageToReplace.imageMapper.getReplacementMode().resize(
			imageToReplace.imageMapper.getValue(),
			imageToReplace.imageMapper.getTargetFormat().name(),
			(int) imageToReplace.toReplace.getAnchor().getWidth(),
			(int) imageToReplace.toReplace.getAnchor().getHeight()
		);
		XSLFPictureData newPictureData = ppt.addPicture(newPictureResized, imageToReplace.imageMapper.getTargetFormat());
		Rectangle2D newImageAnchor = computeNewImageAnchor(
			imageToReplace.toReplace.getAnchor(),
			newPictureResized,
			imageToReplace.imageMapper.getReplacementMode()
		);

		if(shapeContainer instanceof POIXMLDocumentPart) {
			replaceImageInPlace((POIXMLDocumentPart) shapeContainer, imageToReplace, newPictureData, newImageAnchor);
		}
		else if(shapeContainer instanceof XSLFGroupShape) {
			replaceImageInPlace(((XSLFGroupShape) shapeContainer).getSheet(), imageToReplace, newPictureData, newImageAnchor);
		}
		// If the container is not a POIXMLDocumentPart or a XSLFGroupShape,
		// the old image have to deleted along with its properties.
		// The new image will just be place in the same area of the old image.
		// This behavior is a fall back that should not append since
		// I don't think the image container can be something else
		// apart from POIXMLDocumentPart and XSLFGroupShape.
		else {
			XSLFPictureShape newPictureShape = (XSLFPictureShape) shapeContainer.createPicture(newPictureData);
			newPictureShape.setAnchor(newImageAnchor);

			shapeContainer.removeShape(imageToReplace.toReplace);
		}
	}

	/**
	 * Replace an image with another while keeping
	 * all the properties of the old image: z-index, border, shadow...
	 */
	private static void replaceImageInPlace(POIXMLDocumentPart containerDocument, ImageToReplace imageToReplace,
			XSLFPictureData newPictureData, Rectangle2D newImageAnchor) {
		RelationPart rp = containerDocument.addRelation(null, XSLFRelation.IMAGES, newPictureData);
		CTPicture pictureXml = (CTPicture) imageToReplace.toReplace.getXmlObject();
		CTBlip pictureBlip = pictureXml.getBlipFill().getBlip();

		// clean up the old picture data
		PptPoiBridge.removeRelation(containerDocument, containerDocument.getRelationById(pictureBlip.getEmbed()));

		pictureBlip.setEmbed(rp.getRelationship().getId());

		imageToReplace.toReplace.setAnchor(newImageAnchor);
	}

	private static Rectangle2D computeNewImageAnchor(Rectangle2D imageAnchor,
			byte[] newPictureResized,PptImageReplacementMode replacementMode) {
		if(replacementMode == PptImageReplacementMode.RESIZE_CROP) {
			return imageAnchor;
		}

		Dimension newImageSize = ImagesUtils.imageDimension(newPictureResized);
		return new Rectangle2D.Double(
			imageAnchor.getX(),
			imageAnchor.getY(),
			newImageSize.getWidth(),
			newImageSize.getHeight()
		);
	}

	private static boolean processTableShape(XSLFTable tableShape, PptMapper mapper) {
		for(XSLFTableRow row : tableShape.getRows()) {
			for(XSLFTableCell cell : row.getCells()) {
				deleteParagraphsByIndex(
					processTextParagraphs(cell.getTextParagraphs(), mapper),
					((CTTableCell)cell.getXmlObject()).getTxBody()
				);
			}
		}

		return false;
	}

	private static boolean processTextShape(XSLFTextShape textShape, PptMapper mapper) {
		Optional<PptVariable> textVariable = parseHyperlinkVariable(textShape);
		if(shouldHide(textVariable, mapper)) {
			return true;
		}

		deleteParagraphsByIndex(
			processTextParagraphs(textShape.getTextParagraphs(), mapper),
			((CTShape)textShape.getXmlObject()).getTxBody()
		);

		styleShape(textShape, textVariable, mapper);

		return false;
	}

	private static void deleteParagraphsByIndex(List<Integer> indexesToDelete, CTTextBody textBodyXmlNode) {
		int nbDeleted = 0;
		for(Integer indexToDelete : indexesToDelete) {
			textBodyXmlNode.removeP(indexToDelete - nbDeleted++);
		}
	}

	private static List<Integer> processTextParagraphs(List<XSLFTextParagraph> paragraphs, PptMapper mapper) {
		List<Integer> toDelete = new ArrayList<>();
		for (int i=0; i<paragraphs.size(); i++) {
			XSLFTextParagraph paragraph = paragraphs.get(i);
			for (XSLFTextRun textRun : paragraph.getTextRuns()) {
				Optional<PptVariable> parsedHyperlinkVariale = parseHyperlinkVariale(textRun.getHyperlink());

				if(shouldHide(parsedHyperlinkVariale, mapper)) {
					if(paragraph.getTextRuns().size() == 1) {
						toDelete.add(i);
					} else {
						textRun.setText("");
						if(paragraph.getText().trim().isEmpty()) {
							toDelete.add(i);
						}
					}
				} else if(parsedHyperlinkVariale.isPresent()) {
					textRun.getXmlObject().getRPr().unsetHlinkClick();
				}

				parsedHyperlinkVariale
					.flatMap(variable -> mapper.styleText(variable.getName()))
					.ifPresent(styler -> styler.accept(parsedHyperlinkVariale.get().getArg1(), textRun));
			}

			PptParser.replaceTextVariable(paragraph, mapper);
		}
		return toDelete;
	}

	private static void styleShape(XSLFSimpleShape simpleShape, Optional<PptVariable> variableOption, PptMapper mapper) {
		variableOption
			.flatMap(variable ->
				mapper
					.styleShape(variable.getName())
					.map(shapeStyler ->
						// pre-fill the bi consumer styler with the variable argument
						(Consumer<XSLFSimpleShape>) shape -> shapeStyler.accept(variable.getArg1(), shape)
					)
			)
			.ifPresent(shapeStyler ->
				shapeStyler.accept(simpleShape)
			);
	}

	private static boolean shouldHide(Optional<PptVariable> variable, PptMapper mapper) {
		return variable
			.flatMap(shapeVariable ->
				mapper.hideMapping(shapeVariable.getName(), shapeVariable.getArg1())
			)
			.orElse(false);
	}

	private static Optional<PptVariable> parseHyperlinkVariable(XSLFSimpleShape simpleShape) {
		Optional<PptVariable> parsedHyperlinkVariale = parseHyperlinkVariale(simpleShape.getHyperlink());

		// if the link is a variable, remove the link
		parsedHyperlinkVariale.ifPresent(variable -> {
			String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr";
			XmlObject[] rs = simpleShape.getXmlObject().selectPath(xquery);
			CTNonVisualDrawingProps nvPr = (CTNonVisualDrawingProps) rs[0];
			nvPr.unsetHlinkClick();
		});

		return parsedHyperlinkVariale;
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

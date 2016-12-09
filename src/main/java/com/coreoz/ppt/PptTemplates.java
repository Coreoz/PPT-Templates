package com.coreoz.ppt;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import lombok.SneakyThrows;

/**
 * Variable format in the PPT: $/variableName:'argument'/
 */
public class PptTemplates {

	@SneakyThrows
	public XMLSlideShow process(InputStream pptData, PptMapper mapper) {
		try(XMLSlideShow ppt = new XMLSlideShow(pptData)) {
			processPpt(ppt, mapper);
			
			return ppt;
		}
	}
	
	public void processPpt(XMLSlideShow ppt, PptMapper mapper) {
		for(XSLFSlide slide : ppt.getSlides()) {
			Iterator<XSLFShape> shapeIterator = slide.getShapes().iterator();
			while(shapeIterator.hasNext()) {
				if(processShape(ppt, slide, shapeIterator.next(), mapper)) {
					shapeIterator.remove();
				}
			}
		}
	}
	
	// internal
	
	/**
	 * Handles shape modification
	 * @return true is the shape should be removed
	 */
	private boolean processShape(XMLSlideShow ppt, XSLFSlide slide, XSLFShape shape, PptMapper mapper) {
		if(shape instanceof XSLFTextShape) {
			return processTextShape((XSLFTextShape) shape, mapper);
		}
		if(shape instanceof XSLFTable) {
			return processTableShape((XSLFTable) shape, mapper);
		}
		if(shape instanceof XSLFAutoShape) {
			return processGeometryShape((XSLFAutoShape) shape, mapper);
		}
		if(shape instanceof XSLFPictureShape) {
			return processImageShape(ppt, slide, (XSLFPictureShape) shape, mapper);
		}
		return false;
	}
	
	private boolean processImageShape(XMLSlideShow ppt, XSLFSlide slide, XSLFPictureShape imageShape, PptMapper mapper) {
		if(shouldHide(imageShape, mapper)) {
			return true;
		}
		
		
		return false;
	}

	private boolean processGeometryShape(XSLFAutoShape geometryShape, PptMapper mapper) {
		if(shouldHide(geometryShape, mapper)) {
			return true;
		}
		
		processTextParagraphs(geometryShape.getTextParagraphs(), mapper);
		
		return false;
	}
	
	private boolean processTableShape(XSLFTable tableShape, PptMapper mapper) {
		for(XSLFTableRow row : tableShape.getRows()) {
			for(XSLFTableCell cell : row.getCells()) {
				processTextParagraphs(cell.getTextParagraphs(), mapper);
			}
		}
		
		return false;
	}
	
	private boolean processTextShape(XSLFTextShape textShape, PptMapper mapper) {
		if(shouldHide(textShape, mapper)) {
			return true;
		}
		
		processTextParagraphs(textShape.getTextParagraphs(), mapper);
		
		return false;
	}

	private void processTextParagraphs(List<XSLFTextParagraph> paragraphs, PptMapper mapper) {
		for (Iterator<XSLFTextParagraph> iterator = paragraphs.iterator(); iterator.hasNext();) {
			XSLFTextParagraph paragraph = iterator.next();
			
			if(shouldHide(PptParser.parse(paragraph.getText()), mapper)) {
				iterator.remove();
			} else {
				PptParser.replaceTextVariable(paragraph, mapper);
			}
		}
	}
	
	private boolean shouldHide(SimpleShape<?, ?> simpleShape, PptMapper mapper) {
		return shouldHide(parseHyperlinkVariale(simpleShape), mapper);
	}
	
	private boolean shouldHide(Optional<PptVariable> variable, PptMapper mapper) {
		return variable
			.flatMap(shapeVariable ->
				mapper.hideMapping(shapeVariable.getName(), shapeVariable.getArg1())
			)
			.orElse(false);
	}
	
	private Optional<PptVariable> parseHyperlinkVariale(SimpleShape<?, ?> simpleShape) {
		Hyperlink<?, ?> link = simpleShape.getHyperlink();
		if(link != null && link.getTypeEnum() == HyperlinkType.URL) {
			return PptParser.parse(link.getAddress());
		}
		return Optional.empty();
	}
	
}

package com.coreoz.ppt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.junit.Test;

public class PptParserTest {

	@Test
	public void no_variable_content_should_be_untouched() throws IOException {
		try(XMLSlideShow ppt = new XMLSlideShow(PptParserTest.class.getResourceAsStream("/parser/simple_multi_lines.pptx"))) {
			XSLFTextParagraph paragraph = firstParagraph(ppt);
			
			PptParser.replaceTextVariable(paragraph, new PptMapper());
			
			assertThat(paragraph.getText()).isEqualTo("Text on multiple text runs");
		}
	}
	
	@Test
	public void variable_content_with_no_replacement_should_be_untouched() throws IOException {
		try(XMLSlideShow ppt = new XMLSlideShow(PptParserTest.class.getResourceAsStream("/parser/simple_varName_variable.pptx"))) {
			XSLFTextParagraph paragraph = firstParagraph(ppt);
			
			PptParser.replaceTextVariable(paragraph, new PptMapper());
			
			assertThat(paragraph.getText()).isEqualTo("Text with a var: $/varName/");
		}
	}
	
	@Test
	public void variable_content_with_replacement_should_changed() throws IOException {
		try(XMLSlideShow ppt = new XMLSlideShow(PptParserTest.class.getResourceAsStream("/parser/simple_varName_variable.pptx"))) {
			XSLFTextParagraph paragraph = firstParagraph(ppt);
			
			PptParser.replaceTextVariable(paragraph, new PptMapper().text("varName", "replacement"));
			
			assertThat(paragraph.getText()).isEqualTo("Text with a var: replacement");
		}
	}
	
	private XSLFTextParagraph firstParagraph(XMLSlideShow ppt) {
		return ((XSLFTextShape) ppt.getSlides().get(0).getShapes().get(0)).getTextParagraphs().get(0);
	}
	
}

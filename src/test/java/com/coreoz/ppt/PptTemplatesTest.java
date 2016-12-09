package com.coreoz.ppt;

import java.io.FileOutputStream;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import lombok.SneakyThrows;

// not a real test, too hard to setup
public class PptTemplatesTest {

	@SneakyThrows
	public static void main(String[] args) {
		PptMapper mapper = new PptMapper()
			.hide("hidden", arg -> "true".equals(arg))
			.text("var1", "Content replaced")
			.text("var3", "Header cell replaced")
			.text("var4", "Content cell replaced")
			.imageJpg("image", IOUtils.toByteArray(PptTemplatesTest.class.getResourceAsStream("/images/replacedImage.jpg")));
		
		XMLSlideShow transformed = PptTemplates.process(PptTemplatesTest.class.getResourceAsStream("/full_content.pptx"), mapper);
		
		try(FileOutputStream out = new FileOutputStream("full_content_transformed.pptx")) {
			transformed.write(out);
		}
	}
	
}

package com.coreoz.ppt;

import java.util.Optional;

import com.coreoz.ppt.patternMatcher.characterHandler.AbstractCharacterHandler;
import com.coreoz.ppt.patternMatcher.characterHandler.FirstCharacter;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

class PptParser {

	static Optional<PptVariable> parse(String text) {
		if(text.startsWith("$/") && text.endsWith("/")) {
			int indexStartParameter = text.indexOf(':');
			if(indexStartParameter < 0) {
				return Optional.of(PptVariable.of(text.substring(2, text.length() - 1), null));
			}
			return Optional.of(PptVariable.of(
				text.substring(2, indexStartParameter),
				text.substring(indexStartParameter + 2, text.length() - 2)
			));
		}
		return Optional.empty();
	}

	static void replaceTextVariable(XSLFTextParagraph paragraph, PptMapper mapper) {
		AbstractCharacterHandler currentHandler = new FirstCharacter(mapper);

		for(XSLFTextRun textPart : paragraph.getTextRuns()) {

			currentHandler.registerTextPart(textPart);

			currentHandler = handleTextPart(currentHandler, textPart).orElse(currentHandler);
		}
	}

	private static Optional<AbstractCharacterHandler> handleTextPart(AbstractCharacterHandler currentHandler, XSLFTextRun textPart) {
		char[] textPartRaw = textPart.getRawText().trim().toCharArray();
		int indexOfChar = 0;
		AbstractCharacterHandler nextHandler = currentHandler;

		for(char c : textPartRaw) {
            nextHandler = nextHandler.getNextHandler(c);

            nextHandler.processCharacter(c, indexOfChar++, textPart);
        }
		return Optional.ofNullable(nextHandler);
	}

}

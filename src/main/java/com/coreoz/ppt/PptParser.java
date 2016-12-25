package com.coreoz.ppt;

import java.util.Optional;

import com.coreoz.ppt.patternMatcher.PatternMatcher;
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
		PatternMatcher patternMatcher = new PatternMatcher(mapper);

		for(XSLFTextRun textPart : paragraph.getTextRuns()) {

			patternMatcher.registerTextPart(textPart);

			handleTextPart(patternMatcher, textPart);
		}
	}

	protected static void handleTextPart(PatternMatcher patternMatcher, XSLFTextRun textPart) {
		char[] textPartRaw = textPart.getRawText().trim().toCharArray();
		int indexOfChar = 0;

		for(char c : textPartRaw) {
			patternMatcher.processCharacter(c, indexOfChar++, textPart);
        }
	}

}

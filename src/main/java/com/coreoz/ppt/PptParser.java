package com.coreoz.ppt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

class PptParser {
	
	static Optional<PptVariable> parse(String text) {
		// TODO à implémenter
		return Optional.empty();
	}
	
	static void replaceTextVariable(XSLFTextParagraph paragraph, PptMapper mapper) {
		int indexOfStartVariable = -1;
		List<XSLFTextRun> textPartsVariable = null;
		StringBuilder variableName = null;
		State currentState = State.INITIAL;
		
		for(XSLFTextRun textPart : paragraph.getTextRuns()) {
			char[] textPartRaw = textPart.getRawText().trim().toCharArray();
			int indexOfChar = 0;
			
			if(currentState == State.MAY_BE_VARIABLE || currentState == State.START_VARIABLE || currentState == State.VARIABLE) {
				textPartsVariable.add(textPart);
			}
			
			for(char c : textPartRaw) {
				State nextState = process(currentState, c);
				
				switch (nextState) {
				case INITIAL:
					if(currentState != State.INITIAL) {
						indexOfStartVariable = -1;
						textPartsVariable = null;
						variableName = null;
					}
					
					break;
				case MAY_BE_VARIABLE:
					indexOfStartVariable = indexOfChar;
					textPartsVariable = new ArrayList<>();
					textPartsVariable.add(textPart);
					
					break;
				case START_VARIABLE:
					variableName = new StringBuilder();
					
					break;
				case VARIABLE:
					variableName.append(c);
					
					break;
				case END_VARIABLE:
					replaceVariable(
						indexOfStartVariable,
						indexOfChar,
						mapper.textMapping(variableName.toString()),
						textPartsVariable
					);
					break;
				}
				
				indexOfChar++;
				currentState = nextState;
			}
		}
	}
	
	private static void replaceVariable(int indexOfStartVariable, int indexOfEndVariable,
			Optional<String> replacedText, List<XSLFTextRun> textParts) {
		if(!replacedText.isPresent()) {
			return;
		}
		
		for (int i = 0; i < textParts.size(); i++) {
			XSLFTextRun textPart = textParts.get(i);
			if(i == 0) {
				String partContent = textPart.getRawText();
				StringBuilder textPartReplaced = new StringBuilder(partContent.substring(0, indexOfStartVariable));
				textPartReplaced.append(replacedText.get());
				if(textParts.size() == 1) {
					textPartReplaced.append(partContent.substring(indexOfEndVariable));
				}
				textPart.setText(textPartReplaced.toString());
			} else if(i < (textParts.size() - 1)) {
				textPart.setText("");
			} else {
				textPart.setText(textPart.getRawText().substring(indexOfEndVariable + 1));
			}
		}
	}
	
	private static State process(State before, char c) {
		switch (before) {
		case INITIAL:
			if(c == '$') {
				return State.MAY_BE_VARIABLE;
			}
			break;
		case MAY_BE_VARIABLE:
			if(c == '/') {
				return State.START_VARIABLE;
			}
			break;
		case START_VARIABLE:
			if(c != '/') {
				return State.VARIABLE;
			}
			break;
		case VARIABLE:
			if(c == '/') {
				return State.END_VARIABLE;
			}
			return State.VARIABLE;
		case END_VARIABLE:
		}
		
		return State.INITIAL;
	}
	
	private static enum State {
		INITIAL,
		MAY_BE_VARIABLE,
		START_VARIABLE,
		VARIABLE,
		END_VARIABLE
		;
	}

}

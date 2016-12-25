package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * State of patternMatcher when we are reading a variable name
 * Created by ubu on 14/12/16.
 */
public class StateVariableName extends StatePatternMatcher {
    public StateVariableName(PatternMatcher patternMatcher) {
        super(patternMatcher);
    }

    @Override
    public void processCharacter(char character, int indexOfChar, XSLFTextRun textPart) {
        if(character == '/') {
            this.patternMatcher.setState(new StateInitial(this.patternMatcher));
            this.patternMatcher.replaceVariableName(indexOfChar);
        }
        this.patternMatcher.appendVariableName(character);
    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {
        patternMatcher.addTextPartVariable(textPart);
    }
}

package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * State of patternMatcher when start of a variable is encountered
 * Created by ubu on 14/12/16.
 */
public class StateStartVariableName extends StatePatternMatcher {
    public StateStartVariableName(PatternMatcher patternMatcher) {
        super(patternMatcher);
    }

    @Override
    public void processCharacter(char character, int indexOfChar, XSLFTextRun textPart) {
        if(character != '/') {
            this.patternMatcher.setState(new StateVariableName(this.patternMatcher));
            this.patternMatcher.appendVariableName(character);
        } else {
            this.patternMatcher.setState(new StateInitial(this.patternMatcher));
        }
    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {
        patternMatcher.addTextPartVariable(textPart);
    }
}

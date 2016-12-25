package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * State of patternMatcher when we encounter the start of a variable pattern
 * Created by ubu on 14/12/16.
 */
public class StateMayBeVariable extends StatePatternMatcher {
    public StateMayBeVariable(PatternMatcher patternMatcher) {
        super(patternMatcher);
    }

    @Override
    public void processCharacter(char character, int indexOfChar, XSLFTextRun textPart) {
        if(character == '/') {
            this.patternMatcher.setState(new StateStartVariableName(this.patternMatcher));
        } else {
            this.patternMatcher.setState(new StateInitial(this.patternMatcher));
        }
    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {
        patternMatcher.addTextPartVariable(textPart);
    }
}

package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Initial state of patternMatcher
 * Created by ubu on 14/12/16.
 */
public class StateInitial extends StatePatternMatcher {
    public StateInitial(PatternMatcher patternMatcher) {
        super(patternMatcher);
    }

    @Override
    public void processCharacter(char character, int indexOfChar, XSLFTextRun textPart) {
        if(character == '$') {
            this.patternMatcher.setState(new StateMayBeVariable(this.patternMatcher));
            patternMatcher.setIndexOfStartVariable(indexOfChar);
            patternMatcher.addTextPartVariable(textPart);
        }
    }
}

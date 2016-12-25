package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Generic interface for patternMatcher states
 * Created by ubu on 14/12/16.
 */
public abstract class StatePatternMatcher {
    protected PatternMatcher patternMatcher;

    public StatePatternMatcher(PatternMatcher patternMatcher) {
        this.patternMatcher = patternMatcher;
    }

    public abstract void processCharacter(char character, int indexOfChar, XSLFTextRun textPart);

    public void registerTextPart(XSLFTextRun textPart) {
        // Do nothing by default
    }
}

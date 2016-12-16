package com.coreoz.ppt.patternMatcher.characterHandler;

import com.coreoz.ppt.PptMapper;
import com.coreoz.ppt.patternMatcher.PatternRecognized;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Created by ubu on 14/12/16.
 */
public abstract class AbstractCharacterHandler {
    protected PptMapper mapper;
    protected PatternRecognized patternRecognized;

    public AbstractCharacterHandler(PptMapper mapper) {
        this.mapper = mapper;
        this.patternRecognized = new PatternRecognized();
    }

    public AbstractCharacterHandler(PptMapper mapper, PatternRecognized patternRecognized) {
        this.mapper = mapper;
        this.patternRecognized = patternRecognized;
    }

    public abstract AbstractCharacterHandler getNextHandler(char car);

    public abstract void processCharacter(char c, int indexOfChar, XSLFTextRun textPart);

    public abstract void registerTextPart(XSLFTextRun textPart);
}

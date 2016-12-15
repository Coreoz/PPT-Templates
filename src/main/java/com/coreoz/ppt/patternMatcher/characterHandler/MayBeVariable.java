package com.coreoz.ppt.patternMatcher.characterHandler;

import com.coreoz.ppt.PptMapper;
import com.coreoz.ppt.patternMatcher.PatternRecognized;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Created by ubu on 14/12/16.
 */
public class MayBeVariable extends AbstractCharacterHandler {
    public MayBeVariable(PptMapper mapper, PatternRecognized patternRecognized) {
        super(mapper, patternRecognized);
    }

    @Override
    public AbstractCharacterHandler getNextHandler(char car) {
        if(car == '/') {
            return new StartAbstractCharacter(this.mapper, this.patternRecognized);
        }
        return new FirstAbstractCharacter(mapper);
    }

    @Override
    public void processCharacter(char c, int indexOfChar, XSLFTextRun textPart) {
        patternRecognized.setIndexOfStartVariable(indexOfChar);
        patternRecognized.addTextPartVariable(textPart);
    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {
        patternRecognized.addTextPartVariable(textPart);
    }
}

package com.coreoz.ppt.patternMatcher.characterHandler;

import com.coreoz.ppt.PptMapper;
import com.coreoz.ppt.patternMatcher.PatternRecognized;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Created by ubu on 14/12/16.
 */
public class StartAbstractCharacter extends AbstractCharacterHandler {
    public StartAbstractCharacter(PptMapper mapper, PatternRecognized patternRecognized) {
        super(mapper, patternRecognized);
    }

    @Override
    public AbstractCharacterHandler getNextHandler(char car) {
        if(car != '/') {
            return new InsideAbstractCharacter(this.mapper, this.patternRecognized);
        }
        return new FirstAbstractCharacter(mapper);
    }

    @Override
    public void processCharacter(char c, int indexOfChar, XSLFTextRun textPart) {

    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {
        patternRecognized.addTextPartVariable(textPart);
    }
}

package com.coreoz.ppt.patternMatcher.characterHandler;

import com.coreoz.ppt.PptMapper;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Created by ubu on 14/12/16.
 */
public class FirstCharacter extends AbstractCharacterHandler {
    public FirstCharacter(PptMapper mapper) {
        super(mapper);
    }

    public AbstractCharacterHandler getNextHandler(char car) {
        if(car == '$') {
            return new MayBeVariable(this.mapper, this.patternRecognized);
        }
        return new FirstCharacter(mapper);
    }

    @Override
    public void processCharacter(char c, int indexOfChar, XSLFTextRun textPart) {

    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {

    }
}

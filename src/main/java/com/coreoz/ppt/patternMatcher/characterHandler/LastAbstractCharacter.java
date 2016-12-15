package com.coreoz.ppt.patternMatcher.characterHandler;

import com.coreoz.ppt.PptMapper;
import com.coreoz.ppt.patternMatcher.PatternRecognized;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.util.Optional;

/**
 * Created by ubu on 14/12/16.
 */
public class LastAbstractCharacter extends AbstractCharacterHandler {
    public LastAbstractCharacter(PptMapper mapper, PatternRecognized patternRecognized) {
        super(mapper, patternRecognized);
    }

    @Override
    public AbstractCharacterHandler getNextHandler(char car) {
        return new FirstAbstractCharacter(mapper);
    }

    @Override
    public void processCharacter(char c, int indexOfChar, XSLFTextRun textPart) {
        Optional<String> replacedText = mapper.textMapping(patternRecognized.getVariableName());
        replacedText.ifPresent(text -> replaceVariable(indexOfChar, text));
    }

    private void replaceVariable(int indexOfChar, String replacedText) {
        for (int i = 0; i < patternRecognized.getTextPartsVariable().size(); i++) {
            XSLFTextRun textPart_temp = patternRecognized.getTextPartsVariable().get(i);
            if(i == 0) {
                String partContent = textPart_temp.getRawText();
                StringBuilder textPartReplaced = new StringBuilder(partContent.substring(0, patternRecognized.getIndexOfStartVariable()));
                textPartReplaced.append(replacedText);
                if(patternRecognized.getTextPartsVariable().size() == 1) {
                    textPartReplaced.append(partContent.substring(indexOfChar + 1));
                }
                textPart_temp.setText(textPartReplaced.toString());
            } else if(i < (patternRecognized.getTextPartsVariable().size() - 1)) {
                textPart_temp.setText("");
            } else {
                textPart_temp.setText(textPart_temp.getRawText().substring(indexOfChar + 1));
            }
        }
    }

    @Override
    public void registerTextPart(XSLFTextRun textPart) {

    }
}

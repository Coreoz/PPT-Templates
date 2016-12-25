package com.coreoz.ppt.patternMatcher;

import com.coreoz.ppt.PptMapper;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Match variables names and use the PptMapper to replace them by their values
 * Created by ubu on 15/12/16.
 */
public class PatternMatcher {
    private int indexOfStartVariable;
    private StringBuilder variableName;
    private List<XSLFTextRun> textPartsVariable;
    private PptMapper mapper;

    // Change behavior when state change
    private StatePatternMatcher state;

    public PatternMatcher(PptMapper mapper) {
        this.mapper = mapper;
        this.state = new StateInitial(this);
        this.indexOfStartVariable = 0;
        this.variableName = new StringBuilder();
        this.textPartsVariable = new ArrayList<>();
    }

    void replaceVariableName(int indexOfEndVariable) {
        Optional<String> replacedText = mapper.textMapping(variableName.toString());
        replacedText.ifPresent(text -> replaceVariable(indexOfEndVariable, text));
    }

    private void replaceVariable(int indexOfEndVariable, String replacedText) {
        for (int i = 0; i < textPartsVariable.size(); i++) {
            XSLFTextRun textPart_temp = textPartsVariable.get(i);
            if(i == 0) {
                String partContent = textPart_temp.getRawText();
                StringBuilder textPartReplaced = new StringBuilder(partContent.substring(0, indexOfStartVariable));
                textPartReplaced.append(replacedText);
                if(textPartsVariable.size() == 1) {
                    textPartReplaced.append(partContent.substring(indexOfEndVariable + 1));
                }
                textPart_temp.setText(textPartReplaced.toString());
            } else if(i < (textPartsVariable.size() - 1)) {
                textPart_temp.setText("");
            } else {
                textPart_temp.setText(textPart_temp.getRawText().substring(indexOfEndVariable + 1));
            }
        }
    }

    public void processCharacter(char c, int indexOfChar, XSLFTextRun textPart) {
        this.state.processCharacter(c, indexOfChar, textPart);
    }

    public void registerTextPart(XSLFTextRun textPart) {
        this.state.registerTextPart(textPart);
    }

    void setState(StatePatternMatcher state) {
        this.state = state;
    }

    void setIndexOfStartVariable(int indexOfStartVariable) {
        this.indexOfStartVariable = indexOfStartVariable;
    }

    void appendVariableName(char variableNamePart) {
        this.variableName.append(variableNamePart);
    }

    void addTextPartVariable(XSLFTextRun part) {
        this.textPartsVariable.add(part);
    }
}

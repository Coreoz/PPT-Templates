package com.coreoz.ppt.patternMatcher;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubu on 15/12/16.
 */
public class PatternRecognized {
    private int indexOfStartVariable;
    private StringBuilder variableName;
    private List<XSLFTextRun> textPartsVariable;

    public PatternRecognized() {
        indexOfStartVariable = 0;
        variableName = new StringBuilder();
        textPartsVariable = new ArrayList<>();
    }

    public int getIndexOfStartVariable() {
        return indexOfStartVariable;
    }

    public void setIndexOfStartVariable(int indexOfStartVariable) {
        this.indexOfStartVariable = indexOfStartVariable;
    }

    public String getVariableName() {
        return variableName.toString();
    }

    public void appendVariableName(char variableNamePart) {
        this.variableName.append(variableNamePart);
    }

    public List<XSLFTextRun> getTextPartsVariable() {
        return textPartsVariable;
    }

    public void addTextPartVariable(XSLFTextRun part) {
        this.textPartsVariable.add(part);
    }
}

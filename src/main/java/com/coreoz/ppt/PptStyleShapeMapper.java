package com.coreoz.ppt;

import java.util.function.BiConsumer;

import org.apache.poi.xslf.usermodel.XSLFSimpleShape;

import lombok.Value;

@Value(staticConstructor = "of")
class PptStyleShapeMapper {

	private final BiConsumer<String, XSLFSimpleShape> applyStyle;

}

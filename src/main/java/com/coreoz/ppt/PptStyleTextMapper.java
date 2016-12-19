package com.coreoz.ppt;

import java.util.function.BiConsumer;

import org.apache.poi.sl.usermodel.TextRun;

import lombok.Value;

@Value(staticConstructor = "of")
class PptStyleTextMapper {

	private final BiConsumer<String, TextRun> applyStyle;

}

package com.coreoz.ppt;

import lombok.Value;
import org.apache.poi.sl.usermodel.TextRun;

import java.util.function.BiConsumer;

@Value(staticConstructor = "of")
class PptStyleTextMapper {
	BiConsumer<String, TextRun> applyStyle;
}

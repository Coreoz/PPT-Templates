package com.coreoz.ppt;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.poi.xslf.usermodel.XSLFTextRun;

import lombok.Value;

@Value(staticConstructor = "of")
class PptTextMapper {

	private final Object value;
	private final Supplier<?> supplierToValue;
	private final Function<String, ?> functionToValue;
	private final Consumer<XSLFTextRun> customValue;
	
}

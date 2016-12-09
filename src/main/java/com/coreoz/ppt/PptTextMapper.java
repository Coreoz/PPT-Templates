package com.coreoz.ppt;

import java.util.function.Supplier;

import lombok.Value;

@Value(staticConstructor = "of")
class PptTextMapper {

	private final Object value;
	private final Supplier<?> supplierToValue;
	
}

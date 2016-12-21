package com.coreoz.ppt;

import java.util.function.Function;

import lombok.Value;

@Value(staticConstructor = "of")
class PptTextMapper {

	private final Object value;
	private final Function<String, Object> argumentToValue;

}

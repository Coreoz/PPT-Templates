package com.coreoz.ppt;

import java.util.function.Function;

import lombok.Value;

@Value(staticConstructor = "of")
class PptTextMapper {
	Object value;
	Function<String, Object> argumentToValue;
}

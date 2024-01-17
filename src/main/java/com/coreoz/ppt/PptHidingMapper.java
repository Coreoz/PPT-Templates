package com.coreoz.ppt;

import java.util.function.Predicate;

import lombok.Value;

@Value(staticConstructor = "of")
class PptHidingMapper {
	Predicate<String> shouldHide;
}

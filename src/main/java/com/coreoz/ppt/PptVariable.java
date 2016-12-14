package com.coreoz.ppt;

import lombok.Value;

@Value(staticConstructor = "of")
class PptVariable {

	private final String name;
	private final String arg1;

}

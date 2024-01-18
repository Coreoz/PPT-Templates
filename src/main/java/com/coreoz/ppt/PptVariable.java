package com.coreoz.ppt;

import lombok.Value;

@Value(staticConstructor = "of")
class PptVariable {
	String name;
	String arg1;
}

package com.ctrip.ops.sysdev.filters;

public class StringConverter implements ConverterI {

	public Object convert(Object from) {
		return from.toString();
	}
}

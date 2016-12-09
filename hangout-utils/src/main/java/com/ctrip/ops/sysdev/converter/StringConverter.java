package com.ctrip.ops.sysdev.converter;

public class StringConverter implements Converter {

	public Object convert(Object from) {
		return from.toString();
	}
}

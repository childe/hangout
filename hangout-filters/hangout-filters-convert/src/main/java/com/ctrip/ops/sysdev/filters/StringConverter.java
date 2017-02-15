package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.converter.Converter;

public class StringConverter implements Converter {

	public Object convert(Object from) {
		return from.toString();
	}
}

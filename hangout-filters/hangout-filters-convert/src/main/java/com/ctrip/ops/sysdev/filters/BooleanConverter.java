package com.ctrip.ops.sysdev.filters;

public class BooleanConverter implements ConverterI {

	public Object convert(Object from) {
		if (from instanceof Boolean) {
			return (Boolean) from;
		} else if (from instanceof Number) {
			return ((Number) from).intValue() > 0;
		} else {
			String text = from.toString();

			try {
				Double value = Double.valueOf(text);

				return value.intValue() > 0;
			} catch (NumberFormatException e) {
				// ignore it
			}

			if (text == null || text.length() == 0) {
				return false;
			} else return !("false".equals(text) || "0".equals(text)
					|| "no".equals(text) || "F".equals(text));
		}
	}
}

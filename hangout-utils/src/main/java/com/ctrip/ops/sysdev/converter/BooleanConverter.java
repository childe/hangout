package com.ctrip.ops.sysdev.converter;

public class BooleanConverter implements Converter {

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
			} else if ("false".equals(text) || "0".equals(text)
					|| "no".equals(text) || "F".equals(text)) {
				return false;
			} else {
				return true;
			}
		}
	}
}

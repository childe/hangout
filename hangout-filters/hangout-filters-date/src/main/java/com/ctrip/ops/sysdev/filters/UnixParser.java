package com.ctrip.ops.sysdev.filters;

import org.joda.time.DateTime;

public class UnixParser implements DateParser {

	public DateTime parse(String input) {
		return new DateTime((long) (Double.parseDouble(input) * 1000));
	}

	public static void main(String[] args) {
		String input = "1433238542.48729";
		UnixParser p = new UnixParser();
		System.out.println(p.parse(input));
	}
}

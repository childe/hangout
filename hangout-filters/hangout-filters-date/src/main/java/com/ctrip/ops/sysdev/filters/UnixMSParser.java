package com.ctrip.ops.sysdev.filters;

import org.joda.time.DateTime;

public class UnixMSParser implements DateParser {

	public DateTime parse(String input) {
		// TODO Auto-generated method stub
		return new DateTime(Long.parseLong(input));
	}

	public static void main(String[] args) {
		String input = "1433238542488.29";
		UnixMSParser p = new UnixMSParser();
		System.out.println(p.parse(input));
	}
}

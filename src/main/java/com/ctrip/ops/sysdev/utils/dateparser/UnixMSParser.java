package com.ctrip.ops.sysdev.utils.dateparser;

import org.joda.time.DateTime;

public class UnixMSParser implements DateParser {

	@Override
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

package com.ctrip.ops.sysdev.dataparser;

import org.joda.time.DateTime;

public interface DateParser {
	public DateTime parse(String input);
}

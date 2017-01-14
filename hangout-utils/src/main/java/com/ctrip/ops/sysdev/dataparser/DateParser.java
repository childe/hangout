package com.ctrip.ops.sysdev.dataparser;

import org.joda.time.DateTime;

public interface DateParser {
	DateTime parse(String input);
}

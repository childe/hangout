package com.ctrip.ops.sysdev.filters;

import org.joda.time.DateTime;

public interface DateParser {
	DateTime parse(String input);
}

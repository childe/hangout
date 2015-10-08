package org.ctrip.ops.sysdev.utils.dateparser;

import org.joda.time.DateTime;

public interface DateParser {
	public DateTime parse(String input);
}

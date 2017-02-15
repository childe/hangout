package com.ctrip.ops.sysdev.filters;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ISODateParser implements DateParser {

	private DateTimeFormatter formatter;

	public ISODateParser(String timezone) {

		this.formatter = ISODateTimeFormat.dateTimeParser();

		if (timezone != null) {
			this.formatter = this.formatter.withZone(DateTimeZone
					.forID(timezone));
		} else {
			this.formatter = this.formatter.withOffsetParsed();
		}
	}

	@Override
	public DateTime parse(String input) {
		return this.formatter.parseDateTime(input);
	}
}

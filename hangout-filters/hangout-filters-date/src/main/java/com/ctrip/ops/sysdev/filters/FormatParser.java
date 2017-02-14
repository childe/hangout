package com.ctrip.ops.sysdev.filters;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class FormatParser implements DateParser {

	private DateTimeFormatter formatter;

	public FormatParser(String format, String timezone, String locale) {
		this.formatter = DateTimeFormat.forPattern(format);

		if (timezone != null) {
			this.formatter = this.formatter.withZone(DateTimeZone
					.forID(timezone));
		} else {
			this.formatter = this.formatter.withOffsetParsed();
		}

		if (locale != null) {
			this.formatter = this.formatter.withLocale(Locale
					.forLanguageTag(locale));
		}
	}

	@Override
	public DateTime parse(String input) {
		return this.formatter.parseDateTime(input);
	}

	public static void main(String[] args) {
		LocalDate date = LocalDate.now();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM, yyyy")
				.withLocale(Locale.ENGLISH);
		String str = date.toString(fmt);
		System.out.println(str);

		long a = DateTimeFormat.forPattern("dd MMM YYYY:HH:mm:ss")
				.withLocale(Locale.ENGLISH).parseMillis("13 May 2015:22:46:59");
		System.out.println(a);

		DateTime b = DateTimeFormat.forPattern("dd MMM YYYY:HH:mm:ss")
				.withLocale(Locale.forLanguageTag("en")).parseDateTime("13 May 2015:22:46:59");
		System.out.println(b);


		String input = "2015/05/06 10:31:20.427";
		FormatParser p = new FormatParser("YYYY/MM/dd HH:mm:ss.SSS", null, null);
		System.out.println(p.parse(input));

		input = "13 May 2015:22:46:59";
		p = new FormatParser("dd MMM YYYY:HH:mm:ss", null, "en");
		System.out.println(p.parse(input));
	}
}

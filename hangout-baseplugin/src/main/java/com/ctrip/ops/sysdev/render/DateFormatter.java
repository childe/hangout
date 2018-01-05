package com.ctrip.ops.sysdev.render;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@Log4j2
public class DateFormatter implements TemplateRender {
    private static Pattern p = Pattern.compile("(\\%\\{\\+.*?\\})");
    private static DateTimeFormatter ISOformatter = ISODateTimeFormat
            .dateTimeParser().withOffsetParsed();

    private DateTimeFormatter formatter;
    private String format;
    private String valueFormat;

    public DateFormatter(String format, String timezone) {
        this.format = format;
        Matcher m = p.matcher(format);
        while (m.find()) {
            String match = m.group();
            String dateFormat = match.substring(3, match.length() - 1);
            log.info("date format:" + dateFormat);
            this.formatter = DateTimeFormat.forPattern(dateFormat).withZone(DateTimeZone.forID(timezone));
            this.valueFormat = this.format.substring(0, m.start()) + "%s" + this.format.substring(m.end());
            return;
        }

        log.fatal("could not create date format correctly from " + this.format);
    }

    public Object render(Map event) {
        DateTime timestamp = null;
        Object o = event.get("@timestamp");
        if (o == null) {
            timestamp = new DateTime();
        } else {
            if (o.getClass() == DateTime.class) {
                timestamp = (DateTime) o;
            } else if (o.getClass() == Long.class) {
                timestamp = new DateTime((Long) o);
            } else if (o.getClass() == String.class) {
                timestamp = ISOformatter.parseDateTime((String) o);
            }
        }
        if (timestamp == null) {
            return this.format;
        }
        return String.format(this.valueFormat, timestamp.toString(this.formatter));
    }
}

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
    private static Pattern p = Pattern.compile("(\\%\\{.*?\\})");
    private static DateTimeFormatter ISOformatter = ISODateTimeFormat
            .dateTimeParser().withOffsetParsed();

    private DateTimeZone tz;
    private String format;

    public DateFormatter(String format, String timezone) {
        this.format = format;
        this.tz = DateTimeZone.forID(timezone);
    }

    public Object render(Map event) {
        Matcher m = p.matcher(this.format);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String match = m.group();
            String key = match.substring(2, match.length() - 1);
            if (key.equalsIgnoreCase("+s")) {
                Object o = event.get("@timestamp");
                if (o.getClass() == Long.class) {
                    m.appendReplacement(sb, o.toString());
                }
            } else if (key.startsWith("+")) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern(
                        key.substring(1, key.length())).withZone(this.tz);
                Object o = event.get("@timestamp");
                if (o == null) {
                    DateTime timestamp = new DateTime();
                    m.appendReplacement(sb, timestamp.toString(formatter));
                } else {
                    if (o.getClass() == DateTime.class) {
                        m.appendReplacement(sb,
                                ((DateTime) o).toString(formatter));
                    } else if (o.getClass() == Long.class) {
                        DateTime timestamp = new DateTime((Long) o);
                        m.appendReplacement(sb, timestamp.toString(formatter));
                    } else if (o.getClass() == String.class) {
                        DateTime timestamp = ISOformatter
                                .parseDateTime((String) o);
                        m.appendReplacement(sb, timestamp.toString(formatter));
                    }
                }
            } else if (event.containsKey(key)) {
                m.appendReplacement(sb, (String) event.get(key));
            }

        }
        m.appendTail(sb);

        return sb.toString();
    }
}

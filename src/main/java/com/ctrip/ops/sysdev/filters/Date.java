package com.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.utils.dateparser.DateParser;
import com.ctrip.ops.sysdev.utils.dateparser.FormatParser;
import com.ctrip.ops.sysdev.utils.dateparser.ISODateParser;
import com.ctrip.ops.sysdev.utils.dateparser.UnixMSParser;
import com.ctrip.ops.sysdev.utils.dateparser.UnixParser;

public class Date extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Date.class.getName());

	private String src;
	private String target;
	private boolean addYear;
	private List<DateParser> parsers;

	public Date(Map config) {
		super(config);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {
		if (config.containsKey("src")) {
			this.src = (String) config.get("src");
		} else {
			this.src = "logtime";
		}

		if (config.containsKey("target")) {
			this.target = (String) config.get("target");
		} else {
			this.target = "@timestamp";
		}

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "datefail";
		}

		if (config.containsKey("add_year")) {
			this.addYear = (boolean) config.get("add_year");
		} else {
			this.addYear = false;
		}
		this.parsers = new ArrayList<DateParser>();
		for (String format : (List<String>) config.get("formats")) {
			if (format.equalsIgnoreCase("ISO8601")) {
				parsers.add(new ISODateParser((String) config.get("timezone")));
			} else if (format.equalsIgnoreCase("UNIX")) {
				parsers.add(new UnixParser());
			} else if (format.equalsIgnoreCase("UNIX_MS")) {
				parsers.add(new UnixMSParser());
			} else {
				parsers.add(new FormatParser(format, (String) config
						.get("timezone"), (String) config.get("locale")));
			}
		}
	};

	@Override
	protected Map filter(Map event) {
		if (!event.containsKey(this.src)) {
			return event;
		}

		boolean success = false;

		String input = event.get(this.src).toString();

		if (addYear) {
			input = Calendar.getInstance().get(Calendar.YEAR) + input;
		}

		for (DateParser parser : this.parsers) {
			try {
				event.put(target, parser.parse(input));
				success = true;
				break;
			} catch (Exception e) {
				logger.trace(e.getMessage());
			}
		}

		postProcess(event, success);

		return event;
	};

}

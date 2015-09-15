package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.utils.dateparser.DateParser;
import org.ctrip.ops.sysdev.utils.dateparser.FormatParser;
import org.ctrip.ops.sysdev.utils.dateparser.ISODateParser;
import org.ctrip.ops.sysdev.utils.dateparser.UnixMSParser;
import org.ctrip.ops.sysdev.utils.dateparser.UnixParser;

public class Date extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Date.class.getName());

	private String tagOnFailure;
	private String src;
	private String target;
	private boolean addYear;
	private List<DateParser> parsers;

	private ArrayList<String> removeFields;

	public Date(Map config, ArrayBlockingQueue preQueue) {
		super(config, preQueue);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {
		this.removeFields = (ArrayList<String>) this.config
				.get("remove_fields");

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
			} else if (format.equalsIgnoreCase("UNIXMS")) {
				parsers.add(new UnixMSParser());
			} else {
				parsers.add(new FormatParser(format, (String) config
						.get("timezone"), (String) config.get("locale")));
			}
		}
	};

	@Override
	protected void filter(Map event) {
		if (!event.containsKey(this.src)) {
			return;
		}

		boolean success = false;

		String input = ((String) event.get(this.src));

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

		if (success == false) {
			if (!event.containsKey("tags")) {
				event.put("tags",
						new ArrayList<String>(Arrays.asList(this.tagOnFailure)));
			} else {
				Object tags = event.get("tags");
				if (tags.getClass() == ArrayList.class
						&& ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
					((ArrayList) tags).add(this.tagOnFailure);
				}
			}
		} else if (this.removeFields != null) {
			for (String f : this.removeFields) {
				event.remove(f);
			}
		}
	};

}

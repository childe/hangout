package com.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.Iterator;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Split extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Split.class
			.getName());

	public Split(Map config) {
		super(config);
	}

	private String source;
	private String target;
	private String split_word;
	protected List<String> needFields;

	protected void prepare() {
		if (!config.containsKey("source")) {
			logger.error("no source configured in message");
			System.exit(1);
		}
		this.source = (String) config.get("source");

		if (!config.containsKey("fields")) {
			logger.error("no fields configured in message");
			System.exit(1);
		}
		this.needFields = (ArrayList<String>) this.config.get("fields");

		if (!config.containsKey("split")) {
			logger.error("no split string config file");
			System.exit(1);
		}
		this.split_word = (String) config.get("split");

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "splitfail";
		}

	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map filter(final Map event) {
		if (!event.containsKey(this.source)) {
			return event;
		}
		boolean success = true;

		try {
			String message = (String)event.get(this.source);
			if ( message == null ) {
				success = false;
			} else {
				List<String> values = new ArrayList<String> (Arrays.asList(message.split(this.split_word)));
				Iterator<String> value_iterator = (Iterator<String>)values.iterator();
				Iterator<String> field_iterator = (Iterator<String>)this.needFields.iterator();
				while (field_iterator.hasNext() && value_iterator.hasNext()) {
					event.put(field_iterator.next(), value_iterator.next());
				}
				while (field_iterator.hasNext()) {
					event.put(field_iterator.next(), "");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());;
			logger.warn(event + "split faild");
			success = false;
		}

		this.postProcess(event, success);

		return event;
	};
}

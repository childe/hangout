package com.ctrip.ops.sysdev.filters;

import java.util.Map;

import org.apache.log4j.Logger;


public class ValueSplit extends BaseFilter {
	private static final Logger logger = Logger.getLogger(ValueSplit.class
			.getName());

	public ValueSplit(Map config) {
		super(config);
	}

    private Map<String, String> split_fields; 

	@SuppressWarnings({ "unchecked" })
	protected void prepare() {
		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "valuesplit fail";
        }

		if (!config.containsKey("fields")) {
			logger.error("no fields configured in message");
		}
		this.split_fields = (Map<String, String>) config.get("fields");
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map filter(final Map event) {
		boolean success = true;

		try {
            for(String field: this.split_fields.keySet()) {
                if (event.containsKey(field)) {
                    String value = this.split_fields.get(field);
                    String event_value = (String)event.get(field);
                    String[] values = event_value.split(value); 
                    event.put(field, values);
                }
            }
		} catch (Exception e) {
			logger.error(e.getMessage());;
			logger.warn(event + "valuesplit faild");
			success = false;
		}

		this.postProcess(event, success);

		return event;
	};
}

package com.ctrip.ops.sysdev.decoder;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.simple.JSONValue;

public class JsonDecoder implements IDecode {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> decode(final String message) {
		Map<String, Object> event = (HashMap<String, Object>) JSONValue
				.parse(message);

		if (event == null) {
			event = new HashMap<String, Object>() {
				{
					put("message", message);
					put("@timestamp", DateTime.now());
				}
			};
		} else {
			if (!event.containsKey("@timestamp")) {
				event.put("@timestamp", DateTime.now());
			}
		}
		return event;
	}
}

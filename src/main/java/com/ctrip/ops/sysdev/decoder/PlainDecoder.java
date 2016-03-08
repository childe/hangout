package com.ctrip.ops.sysdev.decoder;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class PlainDecoder implements IDecode {

	@Override
	public Map<String, Object> decode(final String message) {
		HashMap<String, Object> event = new HashMap<String, Object>() {
			{
				put("message", message);
				put("@timestamp", DateTime.now());
			}
		};
		return event;
	}
}

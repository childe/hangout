package org.ctrip.ops.sysdev.decoder;

import java.util.HashMap;
import java.util.Map;


public class PlainDecoder implements IDecode {

	@Override
	public Map<String, Object> decode(String message) {
		HashMap<String, Object> event = new HashMap<String, Object>();
		event.put("message", message);
		return event;
	}
}

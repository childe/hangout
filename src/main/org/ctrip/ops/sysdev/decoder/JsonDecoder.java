package org.ctrip.ops.sysdev.decoder;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

public class JsonDecoder implements IDecode {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> decode(String message) {
		return (HashMap<String, Object>) JSONValue.parse(message);
	}
}

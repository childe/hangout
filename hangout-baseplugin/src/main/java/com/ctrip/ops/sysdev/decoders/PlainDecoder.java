package com.ctrip.ops.sysdev.decoders;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class PlainDecoder implements Decode {

	public Map<String, Object> decode(final String message) {
		return createDefaultEvent(message);
	}
}

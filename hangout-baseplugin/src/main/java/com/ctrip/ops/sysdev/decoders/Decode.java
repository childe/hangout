package com.ctrip.ops.sysdev.decoders;

import java.util.Map;

public interface Decode {
	Map<String, Object> decode(String message);
}

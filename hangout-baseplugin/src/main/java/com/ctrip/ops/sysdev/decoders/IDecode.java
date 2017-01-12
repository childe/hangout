package com.ctrip.ops.sysdev.decoders;

import java.util.Map;

public interface IDecode {
	Map<String, Object> decode(String message);
}

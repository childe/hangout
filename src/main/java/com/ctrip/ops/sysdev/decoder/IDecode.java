package com.ctrip.ops.sysdev.decoder;

import java.util.Map;

public interface IDecode {
	public Map<String, Object> decode(String message);
}

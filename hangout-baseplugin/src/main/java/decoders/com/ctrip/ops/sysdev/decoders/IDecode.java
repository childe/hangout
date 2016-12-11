package decoders.com.ctrip.ops.sysdev.decoders;

import java.util.Map;

public interface IDecode {
	public Map<String, Object> decode(String message);
}

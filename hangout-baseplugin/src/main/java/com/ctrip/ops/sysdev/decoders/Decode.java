package com.ctrip.ops.sysdev.decoders;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public interface Decode {
    Map<String, Object> decode(String message);

    default Map<String, Object> createDefaultEvent(String message) {
        return new HashMap<String, Object>() {
            {
                put("message", message);
                put("@timestamp", DateTime.now());
            }
        };
    }
}

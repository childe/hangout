package com.ctrip.ops.sysdev.decoders;

import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class JsonDecoder implements Decode {

    @SuppressWarnings("unchecked")
    public Map<String, Object> decode(final String message) {
        Map<String, Object> event;
        try {
            event = (HashMap) JSONValue
                    .parseWithException(message);
        } catch (Exception e) {
            log.warn("failed to json parse message to event");
            log.warn(e.getLocalizedMessage());
            event = new HashMap<String, Object>() {
                {
                    put("message", message);
                    put("@timestamp", DateTime.now());
                }
            };
            return event;
        }

        if (event == null) {
            event = new HashMap<String, Object>() {
                {
                    put("message", message);
                    put("@timestamp", DateTime.now());
                }
            };
        }
        return event;
    }
}

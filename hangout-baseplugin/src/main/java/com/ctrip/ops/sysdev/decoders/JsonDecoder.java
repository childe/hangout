package com.ctrip.ops.sysdev.decoders;

import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

@Log4j
public class JsonDecoder implements Decode {

    public Map<String, Object> decode(final String message) {
        Map<String, Object> event = null;
        try {
            event = (HashMap) JSONValue.parseWithException(message);
        } catch (Exception e) {
            log.debug("failed to json parse message to event", e);
        } finally {
            if (event == null) {
                event = createDefaultEvent(message);
            }
            return event;
        }
    }
}

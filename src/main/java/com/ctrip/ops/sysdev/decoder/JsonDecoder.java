package com.ctrip.ops.sysdev.decoder;

import java.util.HashMap;
import java.util.Map;

import org.jcodings.util.Hash;
import org.joda.time.DateTime;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import org.apache.log4j.Logger;

public class JsonDecoder implements IDecode {
    private static final Logger logger = Logger
            .getLogger(JsonDecoder.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> decode(final String message) {
        Map<String, Object> event = null;
        Object e = null;
        try {
            e = JSONValue
                    .parseWithException(message);
        } catch (ParseException exception) {
            logger.warn("failed to json parse message");
            logger.warn(exception.getLocalizedMessage());
            event = new HashMap<String, Object>() {
                {
                    put("message", message);
                    put("@timestamp", DateTime.now());
                }
            };
        }

        if (event == null) {
            event = new HashMap<String, Object>() {
                {
                    put("message", message);
                    put("@timestamp", DateTime.now());
                }
            };
        } else {
            try {
                event = (HashMap<String, Object>) e;
                if (!event.containsKey("@timestamp")) {
                    event.put("@timestamp", DateTime.now());
                }
            } catch (Exception exception) {
                logger.warn("failed to convert event to Map object");
                logger.warn(exception.getLocalizedMessage());
                event = new HashMap<String, Object>() {
                    {
                        put("message", message);
                        put("@timestamp", DateTime.now());
                    }
                };
            }
        }

        return event;
    }
}

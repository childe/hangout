package decoders.com.ctrip.ops.sysdev.decoders;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;

public class JsonDecoder implements IDecode {
    private static final Logger logger = Logger
            .getLogger(JsonDecoder.class.getName());

    @SuppressWarnings("unchecked")
    public Map<String, Object> decode(final String message) {
        Map<String, Object> event = null;
        try {
            event = (HashMap) JSONValue
                    .parseWithException(message);
        } catch (Exception e) {
            logger.warn("failed to json parse message to event");
            logger.warn(e.getLocalizedMessage());
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

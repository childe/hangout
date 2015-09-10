package main.org.ctrip.ops.sysdev.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HangoutConfig {

	public static Map parse(String filename) {
		HashMap configs = new HashMap();

		ArrayList<Map> inputs = new ArrayList<Map>();
		final HashMap kafkaInput = new HashMap();
		kafkaInput.put("topic", "metric_thrift.15s");
		kafkaInput.put("groupID", "hangout");
		kafkaInput.put("zk", "192.168.81.156:2181");
		
		inputs.add(new HashMap() {
			{
				put("kafka", kafkaInput);
			}
		});
		configs.put("inputs", inputs);

		return configs;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

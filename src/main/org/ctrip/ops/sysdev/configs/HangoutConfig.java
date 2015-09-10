package main.org.ctrip.ops.sysdev.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HangoutConfig {

	public static Map parse(String filename) {
		HashMap configs = new HashMap();

		final HashMap kafkaInput1 = new HashMap();
		kafkaInput1.put("topic", "metric_thrift.15s");
		kafkaInput1.put("groupID", "hangout");
		kafkaInput1.put("zk", "192.168.81.156:2181");

		final HashMap kafkaInput2 = new HashMap();
		kafkaInput2.put("topic", "metric_thrift.1m");
		kafkaInput2.put("groupID", "hangout");
		kafkaInput2.put("zk", "192.168.81.156:2181");

		ArrayList<Map> inputs = new ArrayList<Map>() {
			{
				add(new HashMap() {
					{
						put("kafka", kafkaInput1);
					}
				});
				add(new HashMap() {
					{
						put("kafka", kafkaInput2);
					}
				});
			}
		};
		configs.put("inputs", inputs);

		return configs;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

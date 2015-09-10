package main.org.ctrip.ops.sysdev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import main.org.ctrip.ops.sysdev.configs.HangoutConfig;

public class Main {

	public static void main(String[] args) {
		// parse configure file
		HashMap configs = (HashMap) HangoutConfig.parse("");
		System.out.println(configs);

		// for input in all_inputs
		ArrayList<Map> inputs = (ArrayList<Map>) configs.get("inputs");

		for (Map input : inputs) {
			Iterator<Entry<String, Map>> inputIT = input.entrySet()
					.iterator();

			while (inputIT.hasNext()) {
				Map.Entry<String, Map> inputEntry = inputIT.next();
				String inputType = inputEntry.getKey();
				System.out.println(inputType);
				Map inputConfig = inputEntry.getValue();
				System.out.println(inputConfig);
			}
		}

		// for filter in filters

		// for output in output
	}

}

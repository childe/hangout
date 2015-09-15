package org.ctrip.ops.sysdev.utils.jinfilter;

import java.util.HashMap;
import java.util.Map;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.parse.TokenParser;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TreeParser;

public class FloorFilter implements Filter {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "floor";
	}

	@Override
	public Object filter(Object arg0, JinjavaInterpreter arg1, String... arg2) {
		// TODO Auto-generated method stub
		long a = (long) arg0;
		Object pre = null;
		int current;
		for (int i = 0; i < arg2.length; i++) {
			current = Integer.parseInt(arg2[i]);
			if (a <= current) {
				return pre;
			}
			pre = current;
		}
		return pre;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FloorFilter f = new FloorFilter();

		 Object a;
		 a = f.filter(6, null, "5", "7", "20");
		 System.out.println(a);
		
		 a = f.filter(1, null, "5", "7", "20");
		 System.out.println(a);
		
		 a = f.filter(100, null, "5", "7", "20");
		 System.out.println(a);

		Map<String, Object> bindings = new HashMap<>();
		bindings.put("name", "Jared");
		bindings.put("age", 13);

		Jinjava jinjava = new Jinjava();

		Context c = jinjava.getGlobalContext();
		c.registerFilter(f);
		String template = "{{age|floor('10','20')}}";
		// TokenParser t = new TokenParser(null, template);
		// Node parsedTemplate = TreeParser.parseTree(t);

		Context cc = new Context(c, bindings);
		JinjavaInterpreter interpreter = new JinjavaInterpreter(jinjava, cc,
				null);
		// Node parsedTemplate = interpreter.parse(template);
		String ss = interpreter.render(template);
		System.out.println(ss);

	}
}

package org.ctrip.ops.sysdev.utils.jinfilter;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;

public class JinManager {
	public static final Jinjava jinjava = new Jinjava();
	public static final Context c = jinjava.getGlobalContext();

	static {
		c.registerFilter(new DateFormat());
	}
}

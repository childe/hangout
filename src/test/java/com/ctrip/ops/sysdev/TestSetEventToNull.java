package com.ctrip.ops.sysdev;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSetEventToNull {
	private class Filter {
		public boolean process(Map event) {
			event = null;
			return true;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testGsub() throws UnsupportedEncodingException {
		Filter f = new Filter();
		Map event = new HashMap();
		event.put("a", "a");

		f.process(event);

		Assert.assertNotNull(event);
	}
}

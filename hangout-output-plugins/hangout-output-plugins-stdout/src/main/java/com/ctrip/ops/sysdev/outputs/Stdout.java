package com.ctrip.ops.sysdev.outputs;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;

@Log4j
public class Stdout extends BaseOutput {

	public Stdout(Map config) {
		super(config);
	}

	@Override
	protected void prepare() {

	}

	@Override
	protected void emit(Map event) {
		System.out.println(event);
	}
}

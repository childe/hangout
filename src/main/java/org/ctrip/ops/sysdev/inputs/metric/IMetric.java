package org.ctrip.ops.sysdev.inputs.metric;

/**
 * @author joey.wen 2015/12/24
 */
public interface IMetric {
    Object getValueAndReset();
}

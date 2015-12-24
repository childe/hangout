package org.ctrip.ops.sysdev.inputs;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.decoder.IDecode;
import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.inputs.metric.IMetric;
import org.ctrip.ops.sysdev.outputs.BaseOutput;
import org.json.simple.JSONObject;

import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * memory, gc
 * @author joey.wen 2015/12/24
 */
public class Metric extends BaseInput {

    private static final Logger logger = Logger.getLogger(Metric.class.getName());
    private final static SimpleDateFormat simpleDtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private RuntimeMXBean jvmRT = null;
    private MemoryMXBean jvmMemRT = null;
    private int interval = 60;
    private boolean _processWasCalled = false;
    private String encoding;

    private Map<String, IMetric> metricMap = new HashMap<String, IMetric>();
    private ScheduledExecutorService scheduledExecutorService = null;

    private static class MemoryUsageMetric implements IMetric {
        MemoryUsage _getUsage;
        public MemoryUsageMetric(MemoryUsage getUsage) {
            _getUsage = getUsage;
        }
        @Override
        public Object getValueAndReset() {
            MemoryUsage memUsage = _getUsage;
            HashMap m = new HashMap();
            m.put("maxBytes", memUsage.getMax());
            m.put("committedBytes", memUsage.getCommitted());
            m.put("initBytes", memUsage.getInit());
            m.put("usedBytes", memUsage.getUsed());
            m.put("virtualFreeBytes", memUsage.getMax() - memUsage.getUsed());
            m.put("unusedBytes", memUsage.getCommitted() - memUsage.getUsed());
            return m;
        }
    }

    // canonically the metrics data exported is time bucketed when doing counts.
    // convert the absolute values here into time buckets.
    private static class GarbageCollectorMetric implements IMetric {
        GarbageCollectorMXBean _gcBean;
        Long _collectionCount;
        Long _collectionTime;
        public GarbageCollectorMetric(GarbageCollectorMXBean gcBean) {
            _gcBean = gcBean;
        }

        @Override
        public Object getValueAndReset() {
            Long collectionCountP = _gcBean.getCollectionCount();
            Long collectionTimeP = _gcBean.getCollectionTime();

            Map ret = null;
            if(_collectionCount!=null && _collectionTime!=null) {
                ret = new HashMap();
                ret.put("count", collectionCountP - _collectionCount);
                ret.put("timeMs", collectionTimeP - _collectionTime);
            }

            _collectionCount = collectionCountP;
            _collectionTime = collectionTimeP;
            return ret;
        }
    }

    public Metric(Map config, ArrayList<Map> filters, ArrayList<Map> outputs) throws Exception {
        super(config, filters, outputs);
        this.prepare();
    }

    @Override
    protected void prepare() {
        // get the time interval
        interval = (Integer) config.get("interval");
        simpleDtFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        jvmRT = ManagementFactory.getRuntimeMXBean();
        jvmMemRT = ManagementFactory.getMemoryMXBean();

        metricMap.put("uptimeSec", new IMetric() {
            @Override
            public Object getValueAndReset() {
                return jvmRT.getUptime() / 1000.0;
            }
        });

        metricMap.put("startTimeSecs", new IMetric() {
            @Override
            public Object getValueAndReset() {
                return jvmRT.getStartTime() / 1000.0;
            }
        });

        metricMap.put("jvm.name", new IMetric() {
            @Override
            public Object getValueAndReset() {
                return jvmRT.getName();
            }
        });

        metricMap.put("@timestamp", new IMetric() {
            @Override
            public Object getValueAndReset() {
                return simpleDtFormat.format(new Date());
            }
        });

        metricMap.put("host", new IMetric() {
            @Override
            public Object getValueAndReset() {
                try {
                    return getHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return "127.0.0.1";
            }
        });

        metricMap.put("memory/heap", new MemoryUsageMetric(jvmMemRT.getHeapMemoryUsage()));
        metricMap.put("memory/nonheap", new MemoryUsageMetric(jvmMemRT.getNonHeapMemoryUsage()));

        for(GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            metricMap.put("GC/" + b.getName().replaceAll("\\W", ""), new GarbageCollectorMetric(b));
        }

        if (this.config.containsKey("encoding")) {
            this.encoding = (String) this.config.get("encoding");
        } else {
            this.encoding = "UTF8";
        }if (this.config.containsKey("encoding")) {
            this.encoding = (String) this.config.get("encoding");
        } else {
            this.encoding = "UTF8";
        }
    }

    private class ScheduleTask implements Runnable {

        private IDecode decoder;
        private String encoding;
        private BaseFilter[] filterProcessors;
        private BaseOutput[] outputProcessors;

        public ScheduleTask(Metric metricInput) {
            this.decoder = metricInput.createDecoder();
            this.filterProcessors = metricInput.createFilterProcessors();
            this.outputProcessors = metricInput.createOutputProcessors();
        }

        @Override
        public void run() {
            Map<String, Object> metricObjectMap = new HashMap<String, Object>();
            for (Map.Entry<String, IMetric> entry : metricMap.entrySet()) {
                metricObjectMap.put(entry.getKey(), entry.getValue().getValueAndReset());
            }

            String jsonString = JSONObject.toJSONString(metricObjectMap);
            System.out.println(jsonString);
            try {
                Map<String, Object> event = this.decoder.decode(jsonString);

                if (this.filterProcessors != null) {
                    for (BaseFilter bf : filterProcessors) {
                        if (event == null) {
                            break;
                        }
                        event = bf.process(event);
                    }
                }
                if (event != null) {
                    for (BaseOutput bo : outputProcessors) {
                        bo.process(event);
                    }
                }
            } catch (Exception e) {
                logger.error("process event failed:" + jsonString);
                e.printStackTrace();
                logger.error(e);
            }

        }
    }

    @Override
    public void emit() {
        if (!_processWasCalled) {
            if (scheduledExecutorService == null) {
                scheduledExecutorService = Executors.newScheduledThreadPool(10);
                scheduledExecutorService.scheduleAtFixedRate(new ScheduleTask(this), interval, interval, TimeUnit.SECONDS);
            }

            _processWasCalled = true;
        }
    }


    public String getHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

}

package com.ctrip.ops.sysdev.outputs;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.render.DateFormatter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class Elasticsearch extends BaseOutput {
    private final static int BULKACTION = 20000;
    private final static int BULKSIZE = 15; //MB
    private final static int FLUSHINTERVAL = 10;
    private final static boolean DEFAULTSNIFF = true;
    private final static boolean DEFAULTCOMPRESS = false;

    private String index;
    private String indexTimezone;
    private BulkRequestBuilder bulkRequest;
    private TransportClient esclient;
    private TemplateRender indexTypeRender;
    private TemplateRender idRender;
    private TemplateRender parentRender;
    private TemplateRender routeRender;

    private int bulkActions;
    private int bulkSize;
    private int flushInterval;
    private int concurrentRequests;

    private ScheduledThreadPoolExecutor scheduler;
    private ScheduledFuture<?> scheduledFuture;


    private final AtomicLong executionIdGen = new AtomicLong();


    public Elasticsearch(Map config) {
        super(config);
    }

    protected void prepare() {
        this.index = (String) config.get("index");

        if (config.containsKey("timezone")) {
            this.indexTimezone = (String) config.get("timezone");
        } else {
            this.indexTimezone = "UTC";
        }

        if (config.containsKey("document_id")) {
            String document_id = config.get("document_id").toString();
            try {
                this.idRender = TemplateRender.getRender(document_id);
            } catch (IOException e) {
                log.fatal("could not build tempalte from " + document_id);
                System.exit(1);
            }
        } else {
            this.idRender = null;
        }

        String index_type = "logs";
        if (config.containsKey("index_type")) {
            index_type = config.get("index_type").toString();
        }
        try {
            this.indexTypeRender = TemplateRender.getRender(index_type);
        } catch (IOException e) {
            log.fatal("could not build tempalte from " + index_type);
            System.exit(1);
        }

        if (config.containsKey("document_parent")) {
            String document_parent = config.get("document_parent").toString();
            try {
                this.parentRender = TemplateRender.getRender(document_parent);
            } catch (IOException e) {
                log.fatal("could not build tempalte from " + document_parent);
                System.exit(1);
            }
        } else {
            this.parentRender = null;
        }

        if (config.containsKey("route")) {
            String route = config.get("route").toString();
            try {
                this.routeRender = TemplateRender.getRender(route);
            } catch (IOException e) {
                log.fatal("could not build tempalte from " + route);
                System.exit(1);
            }
        } else {
            this.routeRender = null;
        }

        this.initESClient();

        this.flushInterval = config.containsKey("flush_interval") ? (int) config.get("flush_interval") : FLUSHINTERVAL;
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
                1, EsExecutors.daemonThreadFactory(this.esclient.settings(), "bulk_processor"));
        this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        this.scheduledFuture = this.scheduler.scheduleWithFixedDelay(new Flush(),
                TimeValue.timeValueSeconds(flushInterval).millis(),
                TimeValue.timeValueSeconds(flushInterval).millis(),
                TimeUnit.MILLISECONDS);
    }

    private void initESClient() throws NumberFormatException {

        String clusterName = (String) config.get("cluster");

        boolean sniff = config.containsKey("sniff") ? (boolean) config.get("sniff") : DEFAULTSNIFF;
        boolean compress = config.containsKey("compress") ? (boolean) config.get("compress") : DEFAULTCOMPRESS;

        Settings.Builder settings = Settings.builder()
                .put("client.transport.sniff", sniff)
                .put("transport.tcp.compress", compress)
                .put("cluster.name", clusterName);


        if (config.containsKey("settings")) {
            HashMap<String, Object> otherSettings = (HashMap<String, Object>) this.config.get("settings");
            otherSettings.entrySet().stream().forEach(entry -> settings.put(entry.getKey(), entry.getValue()));
        }
        esclient = new PreBuiltTransportClient(settings.build());

        ArrayList<String> hosts = (ArrayList<String>) config.get("hosts");
        final boolean[] atleastOneNode = {false};
        hosts.stream().map(host -> host.split(":")).forEach(parsedHost -> {
            try {
                String host = parsedHost[0];
                String port = parsedHost.length == 2 ? parsedHost[1] : "9300";
                esclient.addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName(host), Integer.parseInt(port)));
                atleastOneNode[0] = true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        if (!atleastOneNode[0]) {
            log.fatal("none of the configured nodes are available");
            System.exit(1);
        }

        this.bulkActions = config.containsKey("bulk_actions") ? (int) config.get("bulk_actions") : BULKACTION;
        this.bulkSize = config.containsKey("bulk_size") ? (int) config.get("bulk_size") : BULKSIZE;
        this.bulkSize *= 1024 * 1024;

        if (config.containsKey("concurrent_requests")) {
            log.warn("concurrent_requests is deprecated");
        }

        this.bulkRequest = esclient.prepareBulk();
    }

    private void beforeBulk(long executionId) {
        log.info("executionId: " + executionId);
        log.info("numberOfActions: " + this.bulkRequest.numberOfActions());
        log.debug("Hosts:" + esclient.transportAddresses().toString());
    }


    private void afterBulk(long executionId, BulkRequest request, BulkResponse bulkResponse) {
        log.info("bulk done with executionId: " + executionId);
        List<DocWriteRequest> requests = request.requests();
        int toBeTry = 0;
        int totalFailed = 0;
        for (BulkItemResponse item : bulkResponse.getItems()) {
            if (item.isFailed()) {
                switch (item.getFailure().getStatus()) {
                    case TOO_MANY_REQUESTS:
                    case SERVICE_UNAVAILABLE:
                        if (toBeTry == 0) {
                            log.error("bulk has failed item which NEED to retry");
                            log.error(item.getFailureMessage());
                        }
                        toBeTry++;
                        DocWriteRequest r = requests.get(item.getItemId());
                        this.bulkRequest.add((IndexRequest) r);
                        break;
                    default:
                        if (totalFailed == 0) {
                            log.error("bulk has failed item which do NOT need to retry");
                            log.error(item.getFailureMessage());
                        }
                        break;
                }

                totalFailed++;
            }
        }

        if (totalFailed > 0) {
            log.info(totalFailed + " doc failed, " + toBeTry + " need to retry");
        } else {
            log.debug("no failed docs");
        }

        if (toBeTry > 0) {
            try {
                Thread.sleep(toBeTry);
                log.info("sleep " + toBeTry
                        + " millseconds after bulk failure");
            } catch (InterruptedException e) {
                log.debug(e);
            }
        } else {
            log.debug("no docs need to retry");
        }
    }


    private void afterBulk(long executionId, Throwable failure) {
        log.error("bulk " + executionId + " exception: " + failure);
    }

    private synchronized void add(IndexRequest indexRequest) {
        // 其实在bulkActions+1条进来的时候才会触发Bulk请求, 为了更好的处理fail item和exception做的妥协
        if (bulkRequest.numberOfActions() >= this.bulkActions || bulkRequest.request().estimatedSizeInBytes() >= bulkSize) {
            this.execute();
        }

        this.bulkRequest.add(indexRequest);
    }

    private void execute() {
        final long executionId = executionIdGen.incrementAndGet();

        this.beforeBulk(executionId);

        BulkRequest requests = this.bulkRequest.request();

        try {
            BulkResponse bulkResponse = bulkRequest.get();
            this.bulkRequest = this.esclient.prepareBulk();
            this.afterBulk(executionId, requests, bulkResponse);
        } catch (OutOfMemoryError e) {
            log.fatal("out of memory while bulking, exit..");
            System.exit(1);
        } catch (NoNodeAvailableException e) {
            log.info("sleep " + requests.requests().size()
                    + " millseconds after bulk NoNodeAvailableException");
            try {
                Thread.sleep(requests.requests().size());
            } catch (InterruptedException e2) {
                log.debug(e2);
            }
        } catch (Throwable e) {
            this.afterBulk(executionId, e);
            this.bulkRequest = this.esclient.prepareBulk();
        }
    }

    protected void emit(final Map event) {
        String _index = DateFormatter.format(event, index, indexTimezone);
        String _indexType = indexTypeRender.render(event).toString();
        IndexRequest indexRequest;
        if (this.idRender == null) {
            indexRequest = new IndexRequest(_index, _indexType).source(event);
        } else {
            String _id = (String) idRender.render(event);
            indexRequest = new IndexRequest(_index, _indexType, _id).source(event);
        }
        if (this.parentRender != null) {
            indexRequest.parent(parentRender.render(event).toString());
        }
        if (this.routeRender != null) {
            indexRequest.routing(this.routeRender.render(event).toString());
        }

        this.add(indexRequest);
    }

    public void shutdown() {
        log.info("flush docs and then shutdown");
        this.bulkRequest.get("30s");
    }

    class Flush implements Runnable {
        @Override
        public void run() {
            synchronized (Elasticsearch.this) {
                if (bulkRequest.numberOfActions() == 0) {
                    return;
                }
                execute();
            }
        }
    }
}

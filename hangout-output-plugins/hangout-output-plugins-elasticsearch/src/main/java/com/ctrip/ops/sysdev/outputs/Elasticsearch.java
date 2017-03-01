package com.ctrip.ops.sysdev.outputs;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.render.DateFormatter;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.RenderUtils;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j
public class Elasticsearch extends BaseOutput {

    private final static int BULKACTION = 20000;
    private final static int BULKSIZE = 15; //MB
    private final static int FLUSHINTERVAL = 10;
    private final static int CONCURRENTREQSIZE = 0;
    private final static boolean DEFAULTSNIFF = true;
    private final static boolean DEFAULTCOMPRESS = false;

    private String index;
    private String indexTimezone;
    private BulkProcessor bulkProcessor;
    private TransportClient esclient;
    private TemplateRender indexTypeRender;
    private TemplateRender idRender;
    private TemplateRender parentRender;

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

        try {
            this.idRender = RenderUtils.esConfigRender(config, "document_id", null);
            this.parentRender = RenderUtils.esConfigRender(config, "document_parent", null);
            this.indexTypeRender = RenderUtils.esConfigRender(config, "index_type", new FreeMarkerRender("logs", "logs"));
            this.initESClient();
        } catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }

    private void initESClient() throws NumberFormatException {

        String clusterName = (String) config.get("cluster");

        boolean sniff = config.containsKey("sniff") ? (boolean) config.get("sniff") : DEFAULTSNIFF;
        boolean compress = config.containsKey("compress") ? (boolean) config.get("compress") : DEFAULTCOMPRESS;

        Settings settings = Settings.builder()
                .put("client.transport.sniff", sniff)
                .put("transport.tcp.compress", compress)
                .put("cluster.name", clusterName).build();

        esclient = new PreBuiltTransportClient(settings);

        ArrayList<String> hosts = (ArrayList<String>) config.get("hosts");
        hosts.stream().map(host -> host.split(":")).forEach(parsedHost -> {
            try {
                String host = parsedHost[0];
                String port = parsedHost.length == 2 ? parsedHost[1] : "9300";
                esclient.addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName(host), Integer.parseInt(port)));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        int bulkActions = config.containsKey("bulk_action") ? (int) config.get("bulk_action") : BULKACTION;
        int bulkSize = config.containsKey("bulk_size") ? (int) config.get("bulk_size") : BULKSIZE;
        int flushInterval = config.containsKey("flush_interval") ? (int) config.get("flush_interval") : FLUSHINTERVAL;
        int concurrentRequests = config.containsKey("concurrent_requests") ? (int) config.get("concurrent_requests") : CONCURRENTREQSIZE;

        bulkProcessor = BulkProcessor.builder(
                esclient,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info("executionId: " + executionId);
                        log.info("numberOfActions: " + request.numberOfActions());
                        log.debug("Hosts:" + esclient.transportAddresses().toString());
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request,
                                          BulkResponse response) {
                        log.info("bulk done with executionId: " + executionId);
                        List<ActionRequest<?>> requests = request.requests();
                        int toBeTry = 0;
                        int totalFailed = 0;
                        for (BulkItemResponse item : response.getItems()) {
                            if (item.isFailed()) {
                                switch (item.getFailure().getStatus()) {
                                    case TOO_MANY_REQUESTS:
                                    case SERVICE_UNAVAILABLE:
                                        if (toBeTry == 0) {
                                            log.error("bulk has failed item which NEED to retry");
                                            log.error(item.getFailureMessage());
                                        }
                                        toBeTry++;
                                        bulkProcessor.add(requests.get(item.getItemId()));
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
                                log.info("sleep " + toBeTry / 2
                                        + "millseconds after bulk failure");
                                Thread.sleep(toBeTry / 2);
                            } catch (InterruptedException e) {
                                log.error(e);
                            }
                        } else {
                            log.debug("no docs need to retry");
                        }

                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        log.error("bulk got exception: " + failure.getMessage());
                    }
                }).setBulkActions(bulkActions)
                .setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
                .setConcurrentRequests(concurrentRequests).build();
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
        this.bulkProcessor.add(indexRequest);
    }

    public void shutdown() {
        log.info("flush docs and then shutdown");

        //flush immediately
        this.bulkProcessor.flush();

        // await for some time for rest data from kafka
        int flushInterval = 10;
        if (config.containsKey("flush_interval")) {
            flushInterval = (int) config.get("flush_interval");
        }
        try {
            this.bulkProcessor.awaitClose(flushInterval, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("failed to bulk docs before shutdown");
            log.error(e);
        }
    }
}

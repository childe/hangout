package com.ctrip.ops.sysdev.outputs;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.exception.YamlConfigException;
import com.ctrip.ops.sysdev.render.DateFormatter;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.RenderUtils;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@Log4j
public class ElasticsearchHTTP extends BaseOutput {
    private final static int BULKACTION = 20000;

    private String index;
    private String indexTimezone;
    private RestClient restClient;
    private TemplateRender indexTypeRender;
    private TemplateRender idRender;
    private int bulkActions;
    private List<String> hosts;
    private List<Map> actionList = new ArrayList<>();
    private static final String BULKPATH= "_bulk";
    private Boolean isSniff;
    private Sniffer sniffer;

    public ElasticsearchHTTP(Map config) {
        super(config);
    }

    protected void prepare() {
        try {
            this.index = getConfig(config,"index",null,true);
            this.bulkActions = getConfig(config,"bulk_actions", BULKACTION,false);
            this.indexTimezone = getConfig(config,"timezone", "UTC",false);
            this.hosts = (ArrayList<String>)getConfig(config,"hosts",null,true);
            this.idRender = RenderUtils.esConfigRender(config, "document_id", null);
            this.indexTypeRender = RenderUtils.esConfigRender(config, "index_type", new FreeMarkerRender("logs", "logs"));
            this.isSniff = getConfig(config,"sniff",true,false);
            this.initESClient();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void initESClient() throws NumberFormatException,
            UnknownHostException {

        List<HttpHost> httpHostList = hosts.stream().map(hostString -> {
            String[] parsedHost = hostString.split(":");
            String host = parsedHost[0];
            int port = parsedHost.length == 2 ? Integer.valueOf(parsedHost[1]) : 9200;
            return new HttpHost(host, port);
        }).collect(toList());
        List<HttpHost> clusterHosts = unmodifiableList(httpHostList);

        restClient = RestClient.builder(clusterHosts.toArray(new HttpHost[clusterHosts.size()])).build();
        if(this.isSniff){
            sniffer = Sniffer.builder(restClient).build();
        }

    }

    protected void emit(final Map event) {
        String _index = DateFormatter.format(event, index, indexTimezone);
        String _indexType = (String) indexTypeRender.render(event);
        String requestBody;
        Response response = null;

        addActionList(event,_index,_indexType);
        if(this.actionList.size()/2>=this.bulkActions) {
            try {

                requestBody = actionList.stream().map(JSONValue::toJSONString).collect(Collectors.joining("\n"))+"\n";
                log.info(requestBody);
                response = restClient.performRequest(
                        "POST",
                        BULKPATH,
                        Collections.<String, String>emptyMap(),
                        new NStringEntity(
                                requestBody,
                                ContentType.APPLICATION_JSON
                        )
                );
                log.info(response.toString());
            } catch (IOException e) {
                log.error("Bulk index es Error:",e);
                if(response!=null)
                    log.error("Response Code is " + response.getStatusLine().toString());
            } finally {
                actionList.clear();
            }
        }
    }

    private void addActionList(Map event, String _index, String _indexType) {
        Map indexAction = new HashMap(){};
        event.put("@timestamp",event.get("@timestamp").toString());
        Map indexMap = new HashMap() {
            {
                put("_index",_index);
                put("_type",_indexType);
            }
        };
        indexAction.put("index",indexMap);
        actionList.add(indexAction);
        actionList.add(event);
    }

    public void shutdown() {
        log.info("close restClient");
        try {
            sniffer.close();
            restClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

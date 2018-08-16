考虑这样一种WEB服务访问日志:
```
{
  "@timestamp": "2018-01-23T11:11:39",
  "clientip": "192.168.0.100",
  "serverip": "10.0.0.200",
  "uri": "/hello"
}

{
  "@timestamp": "2018-01-23T11:11:39",
  "clientip": "192.168.0.100",
  "serverip": "10.0.0.200",
  "uri": "/"
}

{
  "@timestamp": "2018-01-23T11:11:39",
  "clientip": "192.168.0.100",
  "serverip": "10.0.0.201",
  "uri": "/hello"
}
{
  "@timestamp": "2018-01-23T11:11:39",
  "clientip": "192.168.0.100",
  "serverip": "10.0.0.200",
  "uri": "/hello"
}
{
  "@timestamp": "2018-01-23T11:11:40",
  "clientip": "192.168.0.100",
  "serverip": "10.0.0.200",
  "uri": "/hello"
}
```

我需要得到这样一种聚合结果:
```
{@timestamp="2018-01-23T11:11:39", count=2, clientip=192.168.0.100, serverip=10.0.0.200, url=/hello}
{@timestamp="2018-01-23T11:11:40", count=1, clientip=192.168.0.100, serverip=10.0.0.201, url=/hello}
{@timestamp="2018-01-23T11:11:40", count=1, clientip=192.168.0.100, serverip=10.0.0.200, url=/}
{@timestamp="2018-01-23T11:11:40", count=1, clientip=192.168.0.100, serverip=10.0.0.200, url=/hello}
```

如果数据到ES之后再做聚合, 聚合的层级非常深, 很不友好, 所以希望流式的处理掉.

于是写了这样一个Filter, 配置如下:

```
- com.ctrip.ops.hangout.filter.LinkMetric:
    fieldsLink: 'clientip->serverip->url'
    timestamp: '@timestamp'
    reserveWindow: 1200 #20m
    batchWindow: 300 #5m
    add_fields:
        filtertype: metric
```

- fieldsLink: 层级关系, 类似于ES Aggs里面的一层层的Term Aggs, 用->分隔.
- timestamp: 日志中的时间戳字段, 如果不配置这个字段或者日志中没有这个字段, 则日志按当前处理到的时间处理.  **注意: 时间戳字段需要是joda.Datetime 类型, 也就是hangout的Date filter处理过的.**
- batchWindow: 多久的数据聚合在一起, 单位是秒. 300就是指5分钟内的数据做一批聚合, 而且每5分钟输出一次聚合结果.
- reserveWindow: 在内存中保留过去多久的聚合结果. 20分钟前的数据会丢弃(避免极端情况下无限使用内存).  5分钟之前, 20分钟以内的数据不会丢掉, 而是继续处理. 在下个5分钟后一起输出.
- add_fields: 可选项. 主要为聚合数据打标签, 和原始数据区分开.

## BUG: 
1. 如果时间戳比当前时间更晚, reserveWindow不会生效, 可能会导致内存使用过多.
2. 如果数据不是源源不断的, 最后一批聚合数据不会输出... 因为它不能被触发.

## 安装:

下载https://github.com/childe/hangout-filter-linkmetric/releases/download/0.1/hangout-filters-statmetric-0.1.jar, 复制到hangout/modules下面.

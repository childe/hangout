模仿[logstash](https://www.elastic.co/products/logstash)做的一个应用.

This product includes GeoLite2 data created by MaxMind, available from
[http://www.maxmind.com](http://www.maxmind.com)

我们一直用logstash从Kafka消费数据进ES, 随着数据量的越来越大, 需要的logstash实例和机器也越来越多. 

于是就拿java实现了一下目前我们用到的Logstash的插件.   做为java初学者, 肯定有很多地方写的不好.

- input
	 * kafka
- output
	* elastickseach
	* kafka
	* stdout  
- filter
	* Grok
	* Date 
	* Json
	* Gsub
	* Drop
	* Trim
	* Translate
	* Rename
	* Lowercase
	* Uppercase
	* Remove
	* Add
	* KV
	* URLDecode
	* GeoIP2
	
用一个典型配置做测试, 包括Grok Date AddField If条件判断等, 吞吐量是Logstash的5倍左右 .  
不压测吞吐量, 正常消费的情况下, CPU使用率大概是Logstash的50%到25%.

# 运行
	bin/hangout -f app.yml

记录日志到文件:

	bin/hangout -f app.yml -l /var/log/hangout/app.yml.log

默认日志记录是warn级别, 详细日志:

	bin/hangout -f app.yml -l /var/log/hangout/app.yml.log -vvvv

-v 是info , -vv 是debug , -vvvv 是Trace
		

# 配置
配置在一定程度上也是模仿logstash, 但用的是通用的yaml格式.
因为能力有限, 不能实现logstash的那套语法, if是用了另外的格式.
[可以参考一个配置的示例](https://github.com/childe/hangout/blob/master/example.yml)

## Input
### kafka
从Kafka读数据. 下面示例是说, 开2个线程取app这个topic的数据, 编码格式为plain纯文本.  consumer_settings中的参数, 参考[kafka]官方文档](http://kafka.apache.org/documentation.html#consumerconfigs), 所有参数都可以在这里配置. 比较重要的是group.id, zookeeper.connect这两个, 一定要有.   zookeeper.connect的格式为 hostname1:port1,hostname2:port2,hostname3:port3.

    - Kafka:
        topic: 
          app: 2
        consumer_settings:
          group.id: hangout
          zookeeper.connect: 192.168.1.200:2181
          auto.commit.interval.ms: "1000"
          socket.receive.buffer.bytes: "1048576"
          fetch.message.max.bytes: "1048576"
          num.consumer.fetchers: "4"
        codec: plain


## Filter
### Grok
Grok是为了把一个字符串切割成多个field, 用的是[Joni库](https://github.com/jruby/joni), 不完全支持logstash里面的patterns语法,%{INT:bytes:int}这种语法不支持, 只支持%{INT:bytes},字段类型需要在ES中定义.

会依次匹配match中的正则, 直到有一个成功的.

**注意, 如果正则中的groupname和已有的字段一样, 原来的字段被覆盖**

	src: message #default message
	match:
		- '(?<logtime>\S+) (?<user>\S+) (-|(?<level>\w+))'
		- '(?<logtime>\S+\s+\S+) (?<user>\S+) (-|(?<level>\w+))'
	remove_fields: ['message']
	tag_on_failure: grokfail #default grokfail

### Date
Date是用的[jona-time](http://www.joda.org/joda-time/)做解析和格式化.

会依次匹配formats里面的格式, 直到成功.

	src: logtime # default logtime
	formats:
		- 'ISO8601'
		- 'UNIX'
		- 'UNIX_MS'
		- 'YYYY-MM-dd HH:mm:ss.SSS'
	remove_fields: ['logtime']
	tag_on_failure: datefail # default datefail

### Json
解析json字符串, **如果json里面的字段和原有的字段重复, 原有字段会被覆盖!**

    - Json:
        filed: message # required

### GeoIP2
geoip2用的是maxmind公司的开源数据和算法.
This product includes GeoLite2 data created by MaxMind, available from
[http://www.maxmind.com](http://www.maxmind.com)

geoip2里面可以获取的数据也比较多, 目前我只是用到了country_code country_name city_name latitude longitude location 6个字段.

**注意 geoip2里面不含有city_code值, 后续会增加GeoIP filter, 可以获取city_code值**  
如果不需要, 还是建议用GeoIP2, 据[ELKstack-guide](https://github.com/garyelephant/ELKstack-guide-cn/blob/99550ba5cc4be177db1b6b62037fb77ce55c304f/logstash/develop_logstash_filter_geoip2.md), 速度比GeoIP有速倍的提升.

maxmind也提供了数据的下载 [http://dev.maxmind.com/geoip/geoip2/geolite2/](http://dev.maxmind.com/geoip/geoip2/geolite2/)


    - GeoIP2:
        source: message # required
        target: geoip # default geoip
        database: '/tmp/GeoLite2-City.mmdb'

### Drop
没有额外参数, 配置if使用.

	if:
		- '<#if user?matches("huhan3")>true</#if>'

### IF
应用于filter. 是一个列表, 需要满足列表中的每一个条件, 使用[freemarker](http://freemarker.org/) 模板引擎. 

下面这个例子是添加一个target字段, target的值为url字段的第5个部分(下标从0开始).  
只有在满足以下2个条件的时候才会触发添加字段这个行为.

1. 日志含有url字段
2.  url中包含 "http://images4.c-ctrip.com/target/" 或者 ".c-ctrip.com/images/" 字符串.

```
- Add:
   fields:
     target: '<#assign a=url?split("/")>${a[4]}'
   if:
     - '<#if url??>true</#if>'
     - '<#if url?contains("http://images4.c-ctrip.com/target/")>true<#elseif url?contains(".c-ctrip.com/images/")>true</#if>'
```

### Translate
每隔一段时间, 会重新加载字典. 字典是yaml格式.
用的[snakeyaml](https://bitbucket.org/asomov/snakeyaml)加载yaml文件的, 如果key是整数类型,会首先尝试转换成Integer, 超出Integer范围才会尝试Long.  
但是Json格式的日志会被直接转成Long类型的, 导致不能匹配.

所以yaml里面需要这么写:

	!!java.lang.Long 123: 信用卡
	!!java.lang.Long 345: 借记卡

Tranlate 配置：

	source: user
	target: nick
	dictionary_path: /user-nick.yml
	refresh_interval: 300 # default 300 seconds

### KV
将 a=1&b=2, 或者name=app id=123 type=nginx 这样的字符串拆分成{a:1,b:2}  {name:app, id:123, type:nginx} 等多个字段, 放到日志里面去.

配置如下.  
如果targete有定义, 会把拆分出来字段放在这个字段中, 如果没有定义,放到在顶层.  
trim 是把拆分出来的字段内容做前后修整. 将不需要的字符去掉. 下面的示例就是说把双引号和tag都去掉.   
trimkey和trim类似, 处理的是字段名称.

    - KV:
        source: msg # default message
        target: kv   # default null
        field_split: ' '  # default " "
        value_split: '='  # default "="
        trim: '\t\"'  #default null
        trimkey: '\"'  # default null
        tag_on_failure: "KVfail" # default "KVfail"
        remove_fields: ['msg']

### 其它filter
配置都比较简单, 可以参考[配置文件示例](https://github.com/childe/hangout/blob/master/example.yml)

### freemarker
If 条件和addfileds等插件使用[freemarker](http://freemarker.org/)模板引擎, 速度快, 功能强大.

[在线测试工具](http://freemarker-online.kenshoo.com/)


## Output
### Elasticsearch
使用[bulk api](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html) 批量将数据写入ES.

插入失败的时候, 只对 TOO_MANY_REQUESTS, SERVICE_UNAVAILABLE 这两种错误重试.

参数含义参考[java client bulk api](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/bulk.html)

concurrent_requests设置成大于0的数, 意思着多线程处理, 以我应用的经验,还有是一定OOM风险的. 因为在执行bulk的时候, bulkProcessor还在继续接收新的文档, 如果bulk失败了, 会把失败的actionrequest继续放到bulkProcessor里面. 一段时间内, 由于各种原因一直失败的话, 内存就会越用越多. 

看了一下ES的代码, 现在的bulk就是对原来的bulkRequest简单封装, 又是异步又是回调, 我这种新手有些控制不了, 考虑要不要换回bulkRequest自己控制.

	cluster: prod # cluster name, required
	hosts: # required
		- 192.168.1.100
		- 192.168.1.200:9301
	index: 'hangout-${@timestamp.toString("YYYY.MM.dd")}'
	index_type: logs # default logs
	bulk_actions: 20000 # default 20000
	bulk_size: 15	#default 15
	flush_interval: 10	#default 10
	concurrent_requests: 0	#default 0

### Kafka
	#broker_list format is host1:port1,host2:port2, and the list can be a subset of brokers or a VIP pointing to a subset of brokers.
	broker_list: 192.168.1.100:9092 # required
	topic: test # required

### Stdout
主要测试用吧. 因为配置是yml格式, 所以没有其它条件的话, 需要写成 

    - Stdout: {}

# 开发:
如果需要新的插件, 可以从源码编译重新打包.  
或者编译成jar包, 加入到class path(尚未开发完成).

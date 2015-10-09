模仿[logstash](https://www.elastic.co/products/logstash)做的一个应用.

我们一直用logstash从Kafka消费数据进ES, 随着数据量的越来越大, 需要的logstash实例和机器也越来越多. 

于是就拿java实现了一下目前我们用到的Logstash的插件.   

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
	* 等

用一个典型配置做测试, 包括Grok Date AddField If条件判断等, 吞吐量是Logstash的5倍左右 .  
不压测吞吐量, 正常消费的情况下, CPU使用率大概是Logstash的50%到25%.

# 配置
配置在一定程度上也是模仿logstash, 但用的是通用的yaml格式.
因为能力有限, 不能实现logstash的那套语法, if是用了另外的格式.
[可以参考一个配置的示例](https://github.com/childe/hangout/blob/master/example.yml)

## Input
### kafka

	topic: test # string , no default
	groupID: hangout # string, no default
	zk: 192.1.1.100:2181 #string, no default
	threads: 1 # default 1
	session_timeout: "6000" # string, default 6000
	commit_interval: "60000" # string, auto.commit.interval.ms, default 1 minute.
	codec: plain # string, default json


## Filter
### Grok
Grok是为了把一个字符串切割成多个field, 用的是[Joni库](https://github.com/jruby/joni), 暂不支持logstash里面的patterns.

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

### translate
每隔一段时间, 会重新加载字典. 字典是yaml格式.

	source: user
	target: nick
	dictionary_path: /user-nick.dict # yaml format
	refresh_interval: 300 # default 300 seconds

### freemarker
If 条件和addfileds等插件使用[freemarker](http://freemarker.org/).
[在线测试](http://freemarker-online.kenshoo.com/)

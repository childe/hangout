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

## Filter
### Grok

### Date

### freemarker
If 条件和addfileds等插件使用[freemarker](http://freemarker.org/).
[在线测试](http://freemarker-online.kenshoo.com/)
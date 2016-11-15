/*
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ctrip.ops.sysdev.monitor;

import org.apache.commons.lang.ArrayUtils;

public class SinkCounter extends MonitoredCounterGroup implements
    SinkCounterMBean {

  private static final String COUNTER_CONSUMER_KAKFA_NUMBER =
          "sink.consumer.kafka.count";

  private static final String COUNTER_CONSUMER_KAFKA_TIME =
          "sink.consumer.kafka.time";

  private static final String COUNTER_CONSUMER_KAFKA_EXCEPTION =
          "sink.consumer.kafka.exception";

  private static final String COUNTER_WRITE_ES_NUMBER =
          "sink.write.es.count";

  private static final String COUNTER_WRITE_ES_TIME =
          "sink.write.es.time";

  private static final String COUNTER_WRITE_ES_EXCEPTION =
          "sink.write.es.exception";



  private static final String[] ATTRIBUTES = {
          COUNTER_CONSUMER_KAKFA_NUMBER, COUNTER_CONSUMER_KAFKA_TIME,
          COUNTER_CONSUMER_KAFKA_EXCEPTION, COUNTER_WRITE_ES_NUMBER,
          COUNTER_WRITE_ES_TIME, COUNTER_WRITE_ES_EXCEPTION,
  };


  public SinkCounter(String name) {
    super(MonitoredCounterGroup.Type.SINK, name, ATTRIBUTES);
  }

  public SinkCounter(String name, String[] attributes) {
    super(MonitoredCounterGroup.Type.SINK, name,
            (String[]) ArrayUtils.addAll(attributes, ATTRIBUTES));
  }


  @Override
  public long getConsumerOfKafkaCount() {
    return get(COUNTER_CONSUMER_KAKFA_NUMBER);
  }

  public long incrementConsumerOfKafkaCount() {
    return increment(COUNTER_CONSUMER_KAKFA_NUMBER);
  }

  @Override
  public long getConsumerOfKafkaException() {
    return get(COUNTER_CONSUMER_KAFKA_EXCEPTION);
  }

  public long incrementConsumerOfKafkaException() {
    return increment(COUNTER_CONSUMER_KAFKA_EXCEPTION);
  }


  @Override
  public long getWriteDataToEsCount() {
    return get(COUNTER_WRITE_ES_NUMBER);
  }

  public long incrementWriteDataToEsCount() {
    return increment(COUNTER_WRITE_ES_NUMBER);
  }

  @Override
  public long getWriteDataToEsException() {
    return get(COUNTER_WRITE_ES_EXCEPTION);
  }

  public long incrementWriteDataToEsException() {
    return increment(COUNTER_WRITE_ES_EXCEPTION);
  }

}

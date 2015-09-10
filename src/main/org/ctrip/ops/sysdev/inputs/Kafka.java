package main.org.ctrip.ops.sysdev.inputs;

import java.util.Map;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class Kafka extends BaseInput {

	private class ConsumerTest implements Runnable {
		private KafkaStream m_stream;
		private int m_threadNumber;

		public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
			m_threadNumber = a_threadNumber;
			m_stream = a_stream;
		}

		public void run() {
			ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
			while (it.hasNext())
				System.out.println("Thread " + m_threadNumber + ": "
						+ new String(it.next().message()));
			System.out.println("Shutting down Thread: " + m_threadNumber);
		}
	}

	public Kafka(Map config) {
		super(config);
	}

	public Map emit() {
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

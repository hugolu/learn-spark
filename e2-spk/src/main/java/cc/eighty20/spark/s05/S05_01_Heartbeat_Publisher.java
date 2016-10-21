package cc.eighty20.spark.s05;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.eighty20.e2spks05.events.Heartbeat;

public class S05_01_Heartbeat_Publisher {	
	private static MetricRegistry metrics;
	private static ConsoleReporter reporter;
	private static Meter messageMeter;
	private static Meter messageSizeMeter;
	
	public static void main(String[] args) throws Exception {
		CommandLine cmdLine = process_args(args);
		if(cmdLine == null)
			System.exit(-1);
		
		// 取得Command Line傳入的參數
		String bootstrap_servers = cmdLine.getOptionValue("b", "localhost:9092");
		String topic = cmdLine.getOptionValue("t", "test");
		Float msgs_per_second = Float.parseFloat(cmdLine.getOptionValue("n", "0")); // 設定每秒產生多少筆Event, 如果是"0"或null, 就是沒有限制
		Long msgs_limit = Long.parseLong(cmdLine.getOptionValue("m","0"));            // 設定總共要產生多少筆Event, 如果是"0"或沒有, 就是沒有限制
		Long metrics_rpt_period = Long.parseLong(cmdLine.getOptionValue("r","5"));    // 設定要Report Metrics的時間interval, 預設是每5秒
		
		String username = cmdLine.getOptionValue("u", "xxx");                           // 設定Heartbeat事件的user name
		
		// 設定監控的Metrics
		metrics = new MetricRegistry();
		reporter = ConsoleReporter.forRegistry(metrics).build();
		reporter.start(metrics_rpt_period, TimeUnit.SECONDS); // report every 5 seconds
		
		
		// 監控兩種Metrics
		messageMeter = metrics.meter("Event.Message.Pub.Rate");
		messageSizeMeter = metrics.meter("Event.Message.Pub.Bytes");
				
		// 序列化物件成為JSON字串的ObjectMapper
		ObjectMapper om = new ObjectMapper();
		
		// 產生Kafka client的instance
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrap_servers); // 設定要連接的Kafka集群
		props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"); // 設定要如何序列化key
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer"); // 設定要如何序列化value
		props.put("compression.type", "snappy"); // 設定訊息壓縮格式 (gzip, lz4與snappy)
		
		// 產生Kafka Producer的insance
		Producer<byte[], byte[]> kafkaProducer = new KafkaProducer<>(props);		
		
		long msgs_count = 0;		
		Heartbeat eventData = new Heartbeat(username);		
		 
		// 控制每秒產生筆數
	    long thread_sleep_millis = 0l;
	    if(msgs_per_second>0)
	    	thread_sleep_millis = Math.round(1000 / msgs_per_second);		
		 
		// 送出資料給Kafka
		 try {
			 while(true) {
				 // 產生新的事件(event)資料
				 eventData = genEventData(eventData);	
				 byte[] eventKey = om.writeValueAsBytes(eventData.getName());
				 byte[] eventPayload = om.writeValueAsBytes(eventData);
					
				 // 檢查看有沒有超出最大筆數限定值
				 if(msgs_limit>0 && msgs_count>msgs_limit){
					 kafkaProducer.close();
					 return;
				 }
				 
				 msgs_count++;		 
				 
				 // 產生要拋進Kafka的訊息Key與Payload的byte[]
				 ProducerRecord<byte[], byte[]> message = new ProducerRecord<>(topic, eventKey, eventPayload);
				 
				// 把資料送進Kafka的topic中
				 kafkaProducer.send(message, new Callback() {
	                   public void onCompletion(RecordMetadata metadata, Exception e) {
	                       if(e != null){
	                    	   e.printStackTrace();
	                       } else {
	                    	// 記錄metrics
	          				 messageMeter.mark();
	          				 messageSizeMeter.mark(eventKey.length + eventPayload.length);
	                       }
	                   }
	               });		
				 
				 // 檢查時間
				 if(msgs_per_second>0){
					 Thread.sleep(thread_sleep_millis);
				 } 
			 }
		 } finally {
			 kafkaProducer.close();
		 }
	}
	
	// 用來處理與驗證從Command Line傳入的參數
	private static CommandLine process_args(String[] args){
		// 產生CommandLine傳入參數的parser
		CommandLineParser parser = new DefaultParser();
		
		// 產生CommandLine可以設定的參數
		Options options = new Options();
		options.addOption("b", "bootstrap.servers", true, "kafka broker list (10.37.xxx.1:9092,10.37.xxx.2:9092)" );
		options.addOption("t", "topic", true, "kafka topic name" );
		options.addOption("n", "msgs.per.second", true, "messages publish to kafka per seconds");
		options.addOption("m", "msgs.max.limit", true, "total messages to generate");
		options.addOption("r", "metric.rpt.period", true, "metrics report period (seconds)");
		options.addOption("u", "event.userid", true, "userid for heartbeat event");
		
		try {
			// 解析CommandLine的傳入參數
		    CommandLine cmdLine = parser.parse( options, args );
		    // 驗證傳入參數的完整性
		    if(validate_args(cmdLine)){
				return cmdLine;
			} else {
				// 自動產生CommandLine的參數help說明
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("S05_01_Heartbeat_Publisher", options );
				System.exit(-1);
			}
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		    System.exit(-1);
		}		
		return null;
	}
	
	// 用來驗證從Command Line傳入的參數是否符合規範
	private static boolean validate_args(CommandLine cmdLine){
		if(!cmdLine.hasOption("b") || cmdLine.getOptionValue("b")==null)
			return false;
		
		if(!cmdLine.hasOption("t") || cmdLine.getOptionValue("t")==null)
			return false;
		
		return true;
	}
	
	// 用來產生模擬事件
	private static Heartbeat genEventData(Heartbeat eventData){
		eventData.setEvt_dt(System.currentTimeMillis());
		return eventData;
	}
}

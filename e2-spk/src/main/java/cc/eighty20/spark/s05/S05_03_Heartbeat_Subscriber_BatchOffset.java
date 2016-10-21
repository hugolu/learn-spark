package cc.eighty20.spark.s05;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class S05_03_Heartbeat_Subscriber_BatchOffset {	
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
		String group_id = cmdLine.getOptionValue("g", UUID.randomUUID().toString());
		String msg_show = cmdLine.getOptionValue("v", "false");
		
		Boolean msg_show_flag = false; // 用來決定要不要在console秀出收到的訊息
		try{
			msg_show_flag = Boolean.parseBoolean(msg_show);
		}catch(Exception e){
			//just ignore
		}
		
		Long metrics_rpt_period = Long.parseLong(cmdLine.getOptionValue("r","5"));    // 設定要Report Metrics的時間interval, 預設是每5秒
		
		// 設定監控的Metrics
		metrics = new MetricRegistry();
		reporter = ConsoleReporter.forRegistry(metrics).build();
		reporter.start(metrics_rpt_period, TimeUnit.SECONDS); // report every 5 seconds
		
		
		// 監控兩種Metrics
		messageMeter = metrics.meter("Event.Message.Receive.Rate");
		messageSizeMeter = metrics.meter("Event.Message.Receive.Bytes");
		
		// 產生Kafka client的instance
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrap_servers); // 設定要連接的Kafka集群
		props.put("group.id", group_id); // 設定Consumer Group
		props.put("auto.offset.reset", "earliest");
		props.put("enable.auto.commit","false"); // 當設定為"false"的時候, Consumer的Offset的commit必須要手動的執行 (預設為: true)  
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 設定要如何序列化key
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 設定要如何序列化value
		
		// 產生一個新的Kafka的Consumer實例(instance)
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		
		// 要求去聆聽指定的訊息主題(topic)
		consumer.subscribe(Arrays.asList(topic));
		
		// 定義一個最小批量的資料數
		final int minBatchSize = 50;
		// 產生一個Collection來當Buffer
	    List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
	    
	    try {
			// 進行迴圈來持續取得新的訊息
			while (true) {
			    ConsumerRecords<String, String> records = consumer.poll(100);
			    for (ConsumerRecord<String, String> record : records){
			    	if(msg_show_flag){ // 要不要在console秀出收到的訊息			    		
				    	System.out.println("== Event[Heartbeat] ==");
				    	System.out.println("[partition]: " + record.partition());
				    	System.out.println("[offset]: " + record.offset());
				    	System.out.println("[key]: " + record.key());
				    	System.out.println("[value]: " + record.value());
				    	System.out.println("[timestamp]: " + record.timestamp()); // 這個Property是在Kafka 0.10才有
				    	System.out.println("[timestamp_type]: " + record.timestampType()); // 這個Property是在Kafka 0.10才有
			    	}
			    	
			    	// 歩驟 #1. 把資料先塞進一個Batch的Buffer
			    	buffer.add(record);		            
			    	// 記錄metrics
     				messageMeter.mark();
     				messageSizeMeter.mark(record.serializedKeySize() + record.serializedValueSize());
			    }
			    
			    if (buffer.size() >= minBatchSize) {
		    		//當資料量到達批次量的時候, 進行資料處理
		    		// 把資料塞進Database 		: insertIntoDB (buffer);
			    	// 或把資料寫進Redis  		: upateCache(buffer);
			    	// 或把資料寫進Elasticsearch 	: insertIntoES(buffer);
		    		
		    		// 歩驟 #2. 確認ok之後, 再進行offset的commit
		             consumer.commitSync();
		             for(TopicPartition partition : consumer.assignment()){
		            	 OffsetAndMetadata meta = consumer.committed(partition);
		            	 long last_offset = meta.offset() -1;
		            	 System.out.println("\n****************************************************");
		            	 System.out.println("Partition#"+partition.partition()+", last commit offset is: " + last_offset);
		            	 System.out.println("****************************************************\n");
		             }
		             buffer.clear();
		         }	            
			}
		} catch(Exception e) {
			consumer.close();
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
		options.addOption("g", "group.id", true, "kafka subscriber group id");
		options.addOption("r", "metric.rpt.period", true, "metrics report period (seconds)");
		options.addOption("v", "message.show", true, "show received message on console");
		
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
				formatter.printHelp("S05_03_Heartbeat_Subscriber_BatchOffset", options );
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
		
		if(!cmdLine.hasOption("g") || cmdLine.getOptionValue("t")==null)
			return false;
		
		return true;
	}
	
}

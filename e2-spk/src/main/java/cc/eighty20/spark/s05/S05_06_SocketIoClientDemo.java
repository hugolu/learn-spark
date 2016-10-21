package cc.eighty20.spark.s05;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

/**
 * 這支程式示範如何使用Socket.IO的Java client來連結到Node.js的Socket.IO的服務端.
 * 並進行一些事件的聆聽及相關事件參數的記錄.
 * 
 * @author erhwenkuo@gmail.com
 *
 */
public class S05_06_SocketIoClientDemo {
	public static final String EXIT_COMMAND = "exit";

	public static void main(String[] args) throws Exception {
		
		// 設定Socket.IO的服務URL
		String socetio_url = "http://localhost:8080";
		
		// 產生Socket.IO的client實例
		Socket socket = IO.socket(socetio_url);		
		
		// 官方事件[connect] : 當成功地與Socket.IO的服務建立連線時觸發 (Fired upon a successful connection.)
		// 參數: null
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {			
				System.out.println("EVENT_CONNECT: socketid[" + socket.id() + "]");
				
			}
		});
		
		// 官方事件[connecting] : 當要與Socket.IO的服務後台建立連線時觸發 (Fired before successful connection.)
		// 參數: null
		socket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {	
				System.out.println("EVENT_CONNECTING: event occurred");
			}
		});
				
		// 官方事件[disconnect] : 當與Socket.IO的服務後台連線斷開時觸發 (Fired after connection disconnected.)
		// 參數: 字串"transport error"
		socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {	
				System.out.println("EVENT_DISCONNECT: " + (String) args[0]);
			}
		});
		
		// 官方事件[message] : 當交換頻道["message"]有訊息發佈時被觸發 (Fired after receive message on specific channel[message].)
		// 參數: 客制化物件(根據應用邏輯而有不同)
		socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				System.out.println("EVENT_MESSAGE: " + (String) args[0]);	
			}
		});
		
		
		// 官方事件[connect_error]: 當與Socket.IO的服務連線銾生問題時觸發 (Fired upon a connection error.)
		// 參數: 是"error object" (EngineIOException)
		socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				EngineIOException ioException = (EngineIOException) args[0];
				System.out.println("EVENT_ERROR: " + ioException.toString());		
			}
		});
		
		// 官方事件[connect_error]: 當與Socket.IO的服務連線銾生問題時觸發 (Fired upon a connection error.)
		// 參數: 是"error object" (EngineIOException)
		socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				EngineIOException ioException = (EngineIOException) args[0];
				System.out.println("EVENT_CONNECT_ERROR: " + ioException.toString());					
			}
		});
		
		// 官方事件[connect_timeout]:  Fired upon a connection timeout.
		// 參數: 是"error object"
		socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				System.out.println("EVENT_CONNECT_TIMEOUT: " + args);				
			}
		});
		
		// 官方事件[reconnect]: Fired upon a successful reconnection.
		// 參數: 是經過多少attempt才重新連線成功的次數
		socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				Integer reconnect_count = (Integer) args[0];
				System.out.println("EVENT_RECONNECT: " + reconnect_count);				
			}
		});
		
		// 官方事件[reconnect_error]: Fired upon a reconnection attempt error.
		// 參數: 是"error object"
		socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				SocketIOException ioException = (SocketIOException) args[0];
				System.out.println("EVENT_RECONNECT_ERROR: " + ioException);				
			}
		});
		
		// 官方事件[reconnect_failed]: Fired when couldn’t reconnect within reconnectionAttempts
		socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				System.out.println("EVENT_RECONNECT_FAILED: " + args);				
			}
		});
		
		
		// 官方事件[reconnecting]: Fired upon an attempt to reconnect.
		// 參數: 是"number" : reconnection attempt number
		socket.on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				Integer attempt_count = (Integer)args[0];
				System.out.println("EVENT_RECONNECT_ATTEMPT: " + attempt_count);				
			}
		});
		
		socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				Integer reconnect_count = (Integer)args[0];
				System.out.println("EVENT_RECONNECTING: " + reconnect_count);				
			}
		});
		
		// 官方事件[EVENT_PING]: Fired for heart beat interval occurred.
		// 參數: null
		socket.on(Socket.EVENT_PING, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				System.out.println("EVENT_PING: event occurred");				
			}
		});
		
		// 官方事件[EVENT_PONG]: Fired upon an attempt to reconnect.
		// 參數: 是"number" : PING 與 PONG 兩個事件的時間差
		socket.on(Socket.EVENT_PONG, new Emitter.Listener() {			
			@Override
			public void call(Object... args) {
				Long ping_pong_timegap = (Long) args[0];
				System.out.println("EVENT_PONG: " + ping_pong_timegap);				
			}
		});
		
		
		// *** 執行連線到Socket.IO服務 *** //
		socket.connect();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter some text, or '" + EXIT_COMMAND + "' to quit");
		while(true){
			System.out.print("> ");
			String input = br.readLine();
			System.out.println(input);
			if (input.length() == EXIT_COMMAND.length() && input.toLowerCase().equals(EXIT_COMMAND)) {
				System.out.println("Exiting.");
				
				//進行Socket.IO服務的離線
				socket.disconnect();
				socket.close();
				
			    return;
			}
			System.out.println("...response goes here...");
		}
	}
}

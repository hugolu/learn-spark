package cc.eighty20.e2spks05.events;

import java.util.Date;
import java.util.UUID;

public class Heartbeat{
	private String evt_type = "Heartbeat";	
	private long evt_dt;
	private String name;
	
	public Heartbeat(){
		this.evt_dt = System.currentTimeMillis();
		this.name = UUID.randomUUID().toString();
	}
	
	public Heartbeat(String name){
		this();
		this.name = name;		
	}	
	
	
	public String getEvt_type() {
		return evt_type;
	}

	public void setEvt_type(String evt_type) {
		this.evt_type = evt_type;
	}

	public long getEvt_dt() {
		return evt_dt;
	}

	public void setEvt_dt(long evt_dt) {
		this.evt_dt = evt_dt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Heartbeat [name=" + name + "] @ " + new Date(this.getEvt_dt());
	}	
}

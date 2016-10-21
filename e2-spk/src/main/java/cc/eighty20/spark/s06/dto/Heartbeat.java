package cc.eighty20.spark.s06.dto;

public class Heartbeat {
  private String name;
  private long evt_dt;
  private String evt_type = "heartbeat";

  public Heartbeat() {}

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public long getEvt_dt() {
    return evt_dt;
  }
  public void setEvt_dt(long evt_dt) {
    this.evt_dt = evt_dt;
  }

  public String getEvt_type() {
    return evt_type;
  }
  public void setEvt_type(String evt_type) {
    this.evt_type = evt_type;
  }

  @Override public String toString() {
    return "heartbeat [name=" + name + ", evt_dat=" + evt_dt + ",evt_type=" + evt_type + "]";
  }
}

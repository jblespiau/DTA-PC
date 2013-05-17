package generalNetwork.data;

import com.google.gson.annotations.Expose;

public class JsonDemand {

  @Expose
  public int origin_id;
  @Expose
  public double[] demand;

  public JsonDemand(int orig_id, double[] d) {
    origin_id = orig_id;
    demand = d;
  }
}

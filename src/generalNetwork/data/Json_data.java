package generalNetwork.data;

import com.google.gson.annotations.Expose;

public class Json_data {

  @Expose
  public int max_time_step;
  @Expose
  public double delta_t;

  @Expose
  public JsonDemand[] demands;
  @Expose
  public JsonSplitRatios[] split_ratios;
  
  public Json_data (JsonDemand[] d, JsonSplitRatios[] sr) {
    demands = d;
    split_ratios = sr;
  }
}
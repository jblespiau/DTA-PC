package generalNetwork.data;

import com.google.gson.annotations.Expose;

public class JsonSplitRatios {

  @Expose
  public int node_id;
  @Expose
  public JsonJunctionSplitRatios[] split_ratios;
}

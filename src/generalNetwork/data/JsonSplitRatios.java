package generalNetwork.data;

import com.google.gson.annotations.Expose;

public class JsonSplitRatios {

  @Expose
  public int node_id;
  public JsonJunctionSplitRatios[] split_ratios;
}

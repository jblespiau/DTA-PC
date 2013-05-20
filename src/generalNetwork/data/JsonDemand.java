package generalNetwork.data;

import com.google.gson.annotations.Expose;

/**
 * @brief Describes the demand by giving points on the demand graph.
 * @details This is converted to and from Json. With these data we create
 * 
 * 
 */
public class JsonDemand {

  /* id of the node which is an origin (in the origins list) */
  @Expose
  public int origin_id;
  /* demand[i] is the demand at time delta_t * i */
  @Expose
  public double[] demand;

  public JsonDemand(int orig_id, double[] d) {
    origin_id = orig_id;
    demand = d;
  }
}

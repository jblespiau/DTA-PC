package generalNetwork.data;

import com.google.gson.annotations.Expose;

/**
 * @brief Json representation of the demand for every origin at each time step
 *        and of the internal non-compliant split-ratios.
 */
public class Json_data {

  /*
   * The limitation of the description of the file in terms
   * of time steps. This means that only @a max_time_steps are described in
   * the file
   */
  @Expose
  public int max_time_step;
  /* The time interval between points for the demands and split-ratios */
  @Expose
  public double delta_t;

  /*
   * The description of the demand at every origin.
   * Converted in DemandsFactory
   */
  @Expose
  public JsonDemand[] demands;
  /*
   * The description of the split-ratios at every complex junctions
   * (i.e. not nx1)
   * Converted in ??
   */
  @Expose
  public JsonSplitRatios[] split_ratios;

  public Json_data(JsonDemand[] d, JsonSplitRatios[] sr) {
    demands = d;
    split_ratios = sr;
  }
}
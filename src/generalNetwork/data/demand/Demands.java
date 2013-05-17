package generalNetwork.data.demand;

import generalNetwork.data.JsonDemand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @brief Discretized demands for all time step and origins
 */
public class Demands {

  HashMap<Integer, double[]> demands;

  public Demands(int nb_origins) {
    demands = new HashMap<Integer, double[]>(nb_origins);
  }

  public void put(int origin_id, double[] demand) {
    demands.put(origin_id, demand);
  }

  public double[] get(int origin_id) {
    return demands.get(origin_id);
  }

  public double get(int time_step, int origin_id) {
    return demands.get(origin_id)[time_step];
  }

  /**
   * @return The jsonDemand encoding the demands
   */
  public JsonDemand[] buildJsonDemand() {
    JsonDemand[] result = new JsonDemand[demands.size()];

    Iterator<Entry<Integer, double[]>> iterator =
        demands.entrySet().iterator();
    Entry<Integer, double[]> entry;
    int i = 0;
    while (iterator.hasNext()) {
      entry = iterator.next();
      result[i] = new JsonDemand(entry.getKey(), entry.getValue());
      i++;
    }

    return result;
  }
}
package generalNetwork.data.demand;

import generalLWRNetwork.Origin;
import generalNetwork.data.JsonDemand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @brief Discreet demands for all time step and origins
 */
public class Demands {

  HashMap<Origin, double[]> demands;

  public Demands(int nb_origins) {
    demands = new HashMap<Origin, double[]>(nb_origins);
  }

  public int size() {
    return demands.size();
  }

  public void put(Origin orig, double[] demand) {
    demands.put(orig, demand);
  }

  public double[] get(Origin orig) {
    return demands.get(orig);
  }

  public double get(Origin orig, int time_step) {
    return demands.get(orig)[time_step];
  }

  /**
   * @return The jsonDemand encoding the demands
   */
  public JsonDemand[] buildJsonDemand(int orig_id) {
    JsonDemand[] result = new JsonDemand[demands.size()];

    Iterator<Entry<Origin, double[]>> iterator =
        demands.entrySet().iterator();
    Entry<Origin, double[]> entry;
    int i = 0;
    while (iterator.hasNext()) {
      entry = iterator.next();
      result[i] = new JsonDemand(orig_id, entry.getValue());
      i++;
    }

    return result;
  }

  @Override
  public String toString() {
    String s = "Demands: ";
    Iterator<Entry<Origin, double[]>> it = demands.entrySet().iterator();
    Entry<Origin, double[]> entry;
    while (it.hasNext()) {
      entry = it.next();
      s += entry.getKey().toString() + " -> "
          + Arrays.toString(entry.getValue());
    }

    return s;
  }
}
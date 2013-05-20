package generalNetwork.data.demand;

import generalLWRNetwork.Junction;
import generalNetwork.data.JsonDemand;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dta_solver.Discretization;

/**
 * @brief It maps a DemandFactory for every origin
 */
public class DemandsFactory {

  private Discretization time;
  /* Maps origin_id -> demand for all time steps */
  LinkedHashMap<Integer, DemandFactory> demands;

  /**
   * 
   * @param time
   *          The discretization
   * @param delta_t
   *          The one in the json_format
   * @param json_demands
   *          The array of (id, double[])
   * @param junctions
   *          junctions[i] has to be the junction created from node of id i
   */
  public DemandsFactory(Discretization time, double delta_t,
      JsonDemand[] json_demands,
      Junction[] junctions) {
    this.time = time;
    demands = new LinkedHashMap<Integer, DemandFactory>(json_demands.length);

    DemandFactory tmp;
    for (int i = 0; i < json_demands.length; i++) {
     tmp = new DemandFactory(time);
     for (int k = 0; k < json_demands[i].demand.length; k++) {
        tmp.add(k * delta_t, json_demands[i].demand[k]);
      }
      demands.put(junctions[json_demands[i].origin_id].getUniqueId(),
          tmp);
    }
  }

  public DemandsFactory(Discretization time, int nb_origins) {
    this.time = time;
    demands = new LinkedHashMap<Integer, DemandFactory>(nb_origins);
  }

  public void put(int origin_id, DemandFactory df) {
    demands.put(origin_id, df);
  }

  public void put(int origin_id, int t, double demand) {
    DemandFactory tmp = demands.get(origin_id);

    if (tmp == null) {
      tmp = new DemandFactory(time);
      tmp.add(t, demand);
      demands.put(origin_id, tmp);
      return;
    }
    tmp.add(t, demand);
  }

  public Demands buildDemands() {
    Demands result = new Demands(demands.size());

    Iterator<Entry<Integer, DemandFactory>> iterator =
        demands.entrySet().iterator();
    Entry<Integer, DemandFactory> entry;
    while (iterator.hasNext()) {
      entry = iterator.next();
      result.put(entry.getKey(), entry.getValue().buildDemand());
    }
    return result;
  }

  @Override
  public String toString() {
    return "DemandsFactory [demands=" + demands + "]";
  }
}
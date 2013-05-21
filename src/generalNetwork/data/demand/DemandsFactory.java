package generalNetwork.data.demand;

import generalLWRNetwork.Origin;
import generalNetwork.data.JsonDemand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dta_solver.Discretization;

/**
 * @brief It maps a DemandFactory for every origin
 */
public class DemandsFactory {

  private Discretization time;
  /*
   * Maps Origin -> graph representation of the demand curve for all time
   * steps
   */
  LinkedHashMap<Origin, FunctionGraph> demands;

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
      HashMap<Integer, Origin> node_to_orig) {
    this.time = time;
    demands = new LinkedHashMap<Origin, FunctionGraph>(json_demands.length);

    FunctionGraph tmp;
    for (int i = 0; i < json_demands.length; i++) {
      tmp = new FunctionGraph(time);
      for (int k = 0; k < json_demands[i].demand.length; k++) {
        tmp.add(k * delta_t, json_demands[i].demand[k]);
      }
      demands.put(node_to_orig.get(json_demands[i].origin_id),
          tmp);
    }
  }

  public DemandsFactory(Discretization time, int nb_origins) {
    this.time = time;
    demands = new LinkedHashMap<Origin, FunctionGraph>(nb_origins);
  }

  public void put(Origin orig, FunctionGraph df) {
    demands.put(orig, df);
  }

  public void put(Origin orig, int t, double demand) {
    FunctionGraph tmp = demands.get(orig);

    if (tmp == null) {
      tmp = new FunctionGraph(time);
      tmp.add(t, demand);
      demands.put(orig, tmp);
      return;
    }
    tmp.add(t, demand);
  }

  public Demands buildDemands() {
    Demands result = new Demands(demands.size());

    Iterator<Entry<Origin, FunctionGraph>> iterator =
        demands.entrySet().iterator();
    Entry<Origin, FunctionGraph> entry;
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
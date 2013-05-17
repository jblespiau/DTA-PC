package generalNetwork.state;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class CellInfo {

  public double supply = -1;
  public double demand = -1;
  public double total_density = 0;
  // HashMap<Commodity_id -> corresponding value>
  public LinkedHashMap<Integer, Double> partial_densities;
  public LinkedHashMap<Integer, Double> in_flows;
  public LinkedHashMap<Integer, Double> out_flows;

  public CellInfo() {
    partial_densities = new LinkedHashMap<Integer, Double>();
    in_flows = new LinkedHashMap<Integer, Double>();
    out_flows = new LinkedHashMap<Integer, Double>();
  }

  public CellInfo(LinkedHashMap<Integer, Double> densities) {
    super();

    partial_densities = new LinkedHashMap<Integer, Double>(densities.size());
    in_flows = new LinkedHashMap<Integer, Double>(densities.size());
    out_flows = new LinkedHashMap<Integer, Double>(densities.size());

    partial_densities.putAll(densities);

    Iterator<Double> iterator = densities.values().iterator();
    Double pair;
    while (iterator.hasNext()) {
      pair = iterator.next();
      total_density += pair;
    }
  }

  /* Is it useful ? */
  public void updateInFlows(double total_in_flow) {
    if (total_in_flow == 0) {
      return;
    }
    in_flows.clear();

    Iterator<Entry<Integer, Double>> iterator = partial_densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (iterator.hasNext()) {
      entry = iterator.next();
      in_flows.put(entry.getKey(), entry.getValue() / total_density
          * total_in_flow);
    }
  }

  /* Is it useful ? */
  public void updateOutFlows(double total_out_flow) {
    if (total_out_flow == 0) {
      return;
    }
    out_flows.clear();

    Iterator<Entry<Integer, Double>> iterator = partial_densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (iterator.hasNext()) {
      entry = iterator.next();
      out_flows.put(entry.getKey(), entry.getValue() / total_density
          * total_out_flow);
    }

  }

  public void print() {
        System.out.println("Demand:" + demand);
        System.out.println("Supply:" + supply);
        System.out.println("Densities:" + partial_densities.toString() + "(total: "+ total_density + ")");
        System.out.println("f_in: " + in_flows.toString());
        System.out.println("f_out: " + out_flows.toString());
    }

  public void clearFlow() {
    in_flows.clear();
    out_flows.clear();
  }
}
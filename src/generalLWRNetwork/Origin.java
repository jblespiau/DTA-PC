package generalLWRNetwork;

import generalNetwork.state.Profile;
import generalNetwork.state.splitRatios.HashMapPairDouble;
import generalNetwork.state.splitRatios.PairBufferCommodity;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

class Origin {

  protected Buffer[] entries;
  protected Junction junction;

  protected Origin() {
  }

  public Origin(Junction j, String type, LinkedList<Cell> new_cells,
      LinkedList<Junction> new_junctions) {
    assert j != null;
    assert type != null;
    assert j.getPrev() == null : "An origin junction should have no incoming links";

    if (type.equals("SingleBuffer")) {
      // System.out.println("[Origin.java]Creation of a SingleBuffer origin");

      entries = new Buffer[1];
      Buffer b = new Buffer();
      entries[0] = b;
      new_cells.add(b);

      j.setPrev(new Cell[] { b });
      junction = j;

      // new OriginSingleBuffer(j, type, new_cells, new_junctions);
    } else if (type.equals("MultipleBuffer")) {
      System.out.println("[Origin.java]Creation of a " + type
          + " origin. This type does not exist");
      System.exit(1);

      // new OriginMultipleBuffer(j, type, new_cells, new_junctions);
    } else {
      System.out.println("[Origin.java]Creation of a " + type
          + " origin. This type does not exist");
      System.exit(1);
    }
  }

  public int size() {
    return entries.length;
  }

  public void injectDemand(Profile previous_profile, Profile p, Double demand,
      HashMapPairDouble splits) {

    LinkedHashMap<Integer, Double> previous_densities;
    previous_densities = previous_profile.get(entries[0]).partial_densities;

    LinkedHashMap<Integer, Double> new_densities = new LinkedHashMap<Integer, Double>();

    /* We first put all the previous densities in the new hashmap */
    Iterator<Entry<Integer, Double>> density_iterator = previous_densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (density_iterator.hasNext()) {
      entry = density_iterator.next();
      new_densities.put(new Integer(entry.getKey().intValue()),
          new Double(entry.getValue().doubleValue()));
    }

    /* Then we add the demand for every commodity */
    Iterator<Entry<PairBufferCommodity, Double>> split_iterator = splits
        .entrySet()
        .iterator();
    Entry<PairBufferCommodity, Double> split_entry;
    Double previous_density;
    double total_density = 0;
    int commodity;
    while (split_iterator.hasNext()) {
      split_entry = split_iterator.next();
      commodity = split_entry.getKey().commodity;

      previous_density = new_densities.get(commodity);
      if (previous_density == null) {
        total_density += demand * split_entry.getValue();
        new_densities.put(commodity, demand * split_entry.getValue());
      } else {
        total_density = previous_density + demand * split_entry.getValue();
        new_densities.put(commodity, previous_density + demand
            * split_entry.getValue());
      }
    }

    p.get(entries[0]).total_density = total_density;
    p.get(entries[0]).partial_densities = new_densities;

  }
}

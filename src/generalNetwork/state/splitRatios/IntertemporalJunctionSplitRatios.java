package generalNetwork.state.splitRatios;

import java.util.LinkedHashMap;

/**
 * @brief Saves the intertemporal split ratios.
 * @details The non compliant split ratios which is time dependent is stored
 *          in an array, while the compliant split ratios (which are time
 *          independent) are saved in a HashMap
 */
public class IntertemporalJunctionSplitRatios {

  HashMapPairDouble[] non_compliant_split_ratios;
  LinkedHashMap<Triplet, Double> compliant_split_ratios;

  public IntertemporalJunctionSplitRatios(int total_time_step) {
    non_compliant_split_ratios = new HashMapPairDouble[total_time_step];
    for (int i = 0; i < total_time_step; i++) {
      non_compliant_split_ratios[i] = new HashMapPairDouble();
    }
    compliant_split_ratios = new LinkedHashMap<Triplet, Double>();
  }

  public JunctionSplitRatios get(int time_step) {
    return new JunctionSplitRatios(non_compliant_split_ratios[time_step],
        compliant_split_ratios);
  }

  public void addCompliantSplitRatio(int in_link_id, int out_link_id, int commodity, double split) {
    compliant_split_ratios.put(new Triplet(in_link_id, out_link_id, commodity), split);
  }
}
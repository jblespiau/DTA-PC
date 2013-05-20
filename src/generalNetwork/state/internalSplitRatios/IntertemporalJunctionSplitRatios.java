package generalNetwork.state.internalSplitRatios;

import java.util.Arrays;
import java.util.LinkedHashMap;

import dataStructures.HashMapTripletDouble;
import dataStructures.Triplet;

/**
 * @brief Saves the intertemporal split ratios for a given junction
 * @details The non compliant split ratios which is time dependent is stored
 *          in an array, while the compliant split ratios (which are time
 *          independent) are saved in a HashMap
 */
public class IntertemporalJunctionSplitRatios {

  /** @brief Contains the non compliant split-ratios for all time steps */
  HashMapTripletDouble[] non_compliant_split_ratios;
  /** @brief Contains the compliant split ratios (time independent) */
  LinkedHashMap<Triplet, Double> compliant_split_ratios;

  public IntertemporalJunctionSplitRatios(int total_time_step) {
    non_compliant_split_ratios = new HashMapTripletDouble[total_time_step];
    for (int i = 0; i < total_time_step; i++) {
      non_compliant_split_ratios[i] = new HashMapTripletDouble();
    }
    compliant_split_ratios = new LinkedHashMap<Triplet, Double>();
  }

  public JunctionSplitRatios get(int time_step) {
    return new JunctionSplitRatios(non_compliant_split_ratios[time_step],
        compliant_split_ratios);
  }

  public void addNonCompliantSplitRatio(int k, int in_id, int out_id, int c,
      double split) {
    non_compliant_split_ratios[k].put(new Triplet(in_id, out_id, c), split);
  }

  /**
   * @brief Add a time-independent compliant split ratio
   * @param in_link_id
   *          The id of the incoming link
   * @param out_link_id
   *          The id of the outgoing link
   * @param commodity
   *          The id of the commodity
   * @param split
   *          The value of the split ratio
   */
  public void addCompliantSplitRatio(int in_link_id, int out_link_id,
      int commodity, double split) {
    compliant_split_ratios.put(new Triplet(in_link_id, out_link_id, commodity),
        split);
  }

  @Override
  public String toString() {
    return "IntertemporalJunctionSplitRatios [non_compliant_split_ratios="
        + Arrays.toString(non_compliant_split_ratios)
        + ",\n compliant_split_ratios=" + compliant_split_ratios + "]";
  }

}
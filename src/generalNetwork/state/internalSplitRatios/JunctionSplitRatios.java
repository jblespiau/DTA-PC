package generalNetwork.state.internalSplitRatios;

import java.util.LinkedHashMap;

import dataStructures.HashMapTripletDouble;
import dataStructures.Triplet;

/**
 * @brief Describe the split ratios for a junction at a given time step
 */
public class JunctionSplitRatios {

  public HashMapTripletDouble non_compliant_split_ratios;
  public LinkedHashMap<Triplet, Double> compliant_split_ratios;

  public JunctionSplitRatios(HashMapTripletDouble nc,
      LinkedHashMap<Triplet, Double> c) {
    non_compliant_split_ratios = nc;
    compliant_split_ratios = c;
  }

  /*
   * private void put(Triplet t, double beta) {
   * if (t.commodity != 0)
   * compliant_split_ratios.put(t, beta);
   * else
   * non_compliant_split_ratios.put(t, beta);
   * }
   */

  public double get(Triplet t) {
    if (t.commodity == 0)
      return non_compliant_split_ratios.get(t);
    else
      return compliant_split_ratios.get(t);
  }

  public Double get(int in, int out, int commodity) {
    if (commodity == 0)
      return non_compliant_split_ratios.get(new Triplet(in, out, commodity));
    else
      return compliant_split_ratios.get(new Triplet(in, out, commodity));
  }

  @Override
  public String toString() {
    return "JunctionSplitRatios [non_compliant_split_ratios="
        + non_compliant_split_ratios + ", compliant_split_ratios="
        + compliant_split_ratios + "]";
  }
}
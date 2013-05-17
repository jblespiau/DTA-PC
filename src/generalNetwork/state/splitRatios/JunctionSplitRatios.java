package generalNetwork.state.splitRatios;


import java.util.LinkedHashMap;

/**
 * @brief Describe the split ratios for a junction at a given time step
 */
public class JunctionSplitRatios {

  public HashMapPairDouble non_compliant_split_ratios;
  public LinkedHashMap<Triplet, Double> compliant_split_ratios;

  public JunctionSplitRatios(HashMapPairDouble nc, LinkedHashMap<Triplet, Double> c) {
    non_compliant_split_ratios = nc;
    compliant_split_ratios = c;
  }

  public void put(Triplet t, double beta) {
    compliant_split_ratios.put(t, beta);
  }

  public double get(Triplet t) {
    return compliant_split_ratios.get(t);
  }
  
  public Double get(int in, int out, int commodity) {
    return compliant_split_ratios.get(new Triplet(in, out, commodity));
  }
}

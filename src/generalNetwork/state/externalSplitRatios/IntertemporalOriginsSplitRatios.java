package generalNetwork.state.externalSplitRatios;

import generalLWRNetwork.Origin;

import java.util.HashMap;

import dataStructures.HashMapIntegerDouble;

/**
 * @brief Describe the split ratios for all junctions
 */
public class IntertemporalOriginsSplitRatios {

  HashMap<Origin, IntertemporalOriginSplitRatios> origins_split_ratios;

  /**
   * @brief Creates a new empty representations of all control split-ratios
   * @param nb_origins
   *          The number of origins (in order optimize the memory)
   */
  public IntertemporalOriginsSplitRatios(int nb_origins) {
    origins_split_ratios = new HashMap<Origin, IntertemporalOriginSplitRatios>(
        nb_origins);
  }

  /**
   * @brief Constructs default split-ratios with equally likely compliant flows
   *        representing @a alpha % of the total flow
   */
  private IntertemporalOriginsSplitRatios(int total_time_step,
      Origin[] origins, double alpha) {

    origins_split_ratios = new HashMap<Origin, IntertemporalOriginSplitRatios>(
        origins.length);

    IntertemporalOriginSplitRatios tmp;
    for (int o = 0; o < origins.length; o++) {
      tmp = new IntertemporalOriginSplitRatios(total_time_step);
      tmp.automatic_uniform_distribution(origins[o], alpha);
      origins_split_ratios.put(origins[o], tmp);
    }
  }

  /**
   * @brief Constructs default split-ratios for all time steps with equally
   *        likely compliant flows representing @a alpha % of the total flow
   */
  static public IntertemporalOriginsSplitRatios defaultSplitRatios(
      int total_time_step,
      Origin[] origins, double alpha) {
    return new IntertemporalOriginsSplitRatios(total_time_step, origins, alpha);
  }

  public HashMapIntegerDouble get(Origin origin, int time_step) {
    return origins_split_ratios.get(origin).get(time_step);
  }

  @Override
  public String toString() {
    return "IntertemporalOriginsSplitRatios [origins_split_ratios="
        + origins_split_ratios + "]";
  }
}
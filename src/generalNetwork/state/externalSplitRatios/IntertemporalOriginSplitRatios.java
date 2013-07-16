package generalNetwork.state.externalSplitRatios;

import generalLWRNetwork.Origin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import dataStructures.HashMapIntegerDouble;

/**
 * @brief Describe the split ratios at an origin
 */
public class IntertemporalOriginSplitRatios {

  /*
   * split_ratios[k] are the split ratios for the origin at time step k
   * HashMapPairDouble.get(c) is the split ratio of commodity @a c into the
   * buffer which id is buffer_id
   */
  HashMapIntegerDouble[] split_ratios;

  public IntertemporalOriginSplitRatios(int total_time_step) {
    split_ratios = new HashMapIntegerDouble[total_time_step];
    for (int i = 0; i < total_time_step; i++) {
      split_ratios[i] = new HashMapIntegerDouble();
    }
  }

  public HashMapIntegerDouble get(int time_step) {
    return split_ratios[time_step];
  }

  public void add(int time_step, int commodity, double split) {
    split_ratios[time_step].put(new Integer(commodity), split);
  }

  /**
   * @brief Gives a possible default split_ratios for all time steps
   * @param commodity_1
   *          The id of the first compliant commodity
   * @param commodity_n
   *          The id of the last compliant commodity
   * @param time_step
   *          The total number of time steps
   * @param nb_buffers
   *          The number of buffer in the origin
   * @param alpha
   */
  public void automatic_uniform_distribution(Origin o, double alpha) {

    int time_step = split_ratios.length;
    // Adding the non-compliant split ratios
    if (alpha != 1)
      for (int k = 0; k < time_step; k++)
        add(k, 0, 1.0 - alpha);

    // Adding the compliant split ratios
    LinkedList<Integer> compliant = o.getCompliant_commodities();
    assert compliant != null : "There is no paths leaving the origin "
        + o.getUniqueId();

    Iterator<Integer> it;
    double share = alpha / ((double) (compliant.size()));
    Integer c;
    for (int k = 0; k < time_step; k++) {
      it = compliant.iterator();
      while (it.hasNext()) {
        c = it.next();
        add(k, c, share);
      }
    }
  }

  /**
   * @brief Gives a not physical default split_ratios for all time steps
   * @details The sum of the split ratios at one origin at one time step is
   *          greater than 1
   * @param commodity_1
   *          The id of the first compliant commodity
   * @param commodity_n
   *          The id of the last compliant commodity
   * @param time_step
   *          The total number of time steps
   * @param nb_buffers
   *          The number of buffer in the origin
   * @param alpha
   */
  public void automaticUniformNotPhysicalDistribution(Origin o, double alpha) {

    int time_step = split_ratios.length;
    // Adding the non-compliant split ratios
    if (alpha != 0)
      for (int k = 0; k < time_step; k++)
        add(k, 0, 1.0 - alpha);

    // Adding the compliant split ratios
    LinkedList<Integer> compliant = o.getCompliant_commodities();

    Iterator<Integer> it;
    double share = alpha * 1.1 / ((double) (compliant.size()));
    Integer c;
    for (int k = 0; k < time_step; k++) {
      it = compliant.iterator();
      while (it.hasNext()) {
        c = it.next();
        add(k, c, share);
      }
    }
  }

  @Override
  public String toString() {
    return "IntertemporalOriginSplitRatios [split_ratios="
        + Arrays.toString(split_ratios) + "]";
  }
}
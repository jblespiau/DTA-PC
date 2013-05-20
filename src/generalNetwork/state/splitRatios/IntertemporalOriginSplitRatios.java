package generalNetwork.state.splitRatios;

import java.util.Arrays;

public class IntertemporalOriginSplitRatios {

  /*
   * split_ratios[k] are the split ratios for the origin at time step k
   * HashMapPairDouble.get(Pair(buffer_id, commodity)) is the split ratio of commotidy 
   * into the buffer which id is buffer_id
   */
  HashMapPairDouble[] split_ratios;

  public IntertemporalOriginSplitRatios(int total_time_step) {
    split_ratios = new HashMapPairDouble[total_time_step];
    for (int i = 0; i < total_time_step; i++) {
      split_ratios[i] = new HashMapPairDouble();
    }
  }

  public HashMapPairDouble get(int time_step) {
    return split_ratios[time_step];
  }

  public void add(int time_step, int buffer, int commodity, double split) {
    split_ratios[time_step].put(new PairBufferCommodity(buffer, commodity), split);
  }

  public void automatic_uniform_distribution(int commodity_1, int commodity_n,
      int time_step, int nb_buffers) {
    double share = 1.0 / ((double) (commodity_n - commodity_1 + 1));
    for (int k = 0; k < time_step; k++) {
      for (int c = commodity_1; c <= commodity_n; c++) {
        for (int b = 0; b < nb_buffers; b++) {
          add(k, b, c, share);
        }
      }
    }

  }

  @Override
  public String toString() {
    return "IntertemporalOriginSplitRatios [split_ratios="
        + Arrays.toString(split_ratios) + "]";
  }
}
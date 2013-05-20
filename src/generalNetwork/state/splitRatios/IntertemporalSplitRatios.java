package generalNetwork.state.splitRatios;

import java.util.HashMap;

import generalLWRNetwork.Junction;

/**
 * @brief Contains all the split ratios for all commodities at each junction for
 *        every time step
 */
public class IntertemporalSplitRatios {

  /* Map junction_id -> split_ratios at the junction with this id */
  HashMap<Integer, IntertemporalJunctionSplitRatios> junctions_split_ratios;

  public IntertemporalSplitRatios(Junction[] junctions, int total_time_step) {

    int nb_junctions = junctions.length;
    junctions_split_ratios = new HashMap<Integer, IntertemporalJunctionSplitRatios>();

    for (int i = 0; i < nb_junctions; i++) {
      /* There is no need for split ratios in a merging junction */
      if (!junctions[i].isMergingJunction())
        junctions_split_ratios.put(junctions[i].getUniqueId(),
            new IntertemporalJunctionSplitRatios(total_time_step));
    }
  }

  public JunctionSplitRatios get(int time_step, int junction_id) {
    IntertemporalJunctionSplitRatios sr = junctions_split_ratios
        .get(junction_id);
    if (sr == null)
      return null;
    else
      return junctions_split_ratios.get(junction_id).get(time_step);
  }

  public IntertemporalJunctionSplitRatios get(int junction_id) {
    return junctions_split_ratios.get(junction_id);
  }

  public void addCompliantSRToJunction(int in_id, int out_id, int commodity,
      int split, Junction junction) {
    junctions_split_ratios.get(junction.getUniqueId()).addCompliantSplitRatio(
        in_id, out_id,
        commodity, split);
  }

  @Override
  public String toString() {
    return "IntertemporalSplitRatios \n[junctions_split_ratios=\n"
        + junctions_split_ratios + "]";
  }
}

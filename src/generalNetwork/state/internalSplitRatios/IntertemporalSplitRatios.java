package generalNetwork.state.internalSplitRatios;

import java.util.HashMap;

import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.Junction;
import generalNetwork.data.JsonJunctionSplitRatios;
import generalNetwork.data.JsonSplitRatios;

/**
 * @brief Contains all the split ratios for all commodities at each junction for
 *        every time step
 */
public class IntertemporalSplitRatios {

  /* Map junction_id -> split_ratios at the junction with this id */
  private HashMap<Integer, IntertemporalJunctionSplitRatios> junctions_split_ratios;

  /**
   * @brief Creates an empty representation of the split ratios for all the
   *        junctions and all the time steps
   * @param junctions
   *          The array containing _all_ the junctions of the network
   * @param total_time_step
   *          The number of time steps
   */
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

  /**
   * @brief Set all the internal split ratios for the non-compliant flow
   * @param junctions
   */
  public void addNonCompliantSplitRatios(DiscretizedGraph g,
      JsonSplitRatios[] non_compliant_split_ratios) {
    JsonJunctionSplitRatios[] tmp;
    int node_id;
    
    // For all the junctions
    for (int j = 0; j < non_compliant_split_ratios.length; j++) {
      node_id = non_compliant_split_ratios[j].node_id;
      IntertemporalJunctionSplitRatios ijsr =
          get(g.nodeToJunction(node_id).getUniqueId());

      tmp = non_compliant_split_ratios[j].split_ratios;
      // For all the time steps
      for (int k = 0; k < tmp.length; k++) {
        ijsr.addNonCompliantSplitRatio(
            k,
            g.lastCellofLink(tmp[k].in_id).getUniqueId(),
            g.firstCellofLink(tmp[k].out_id).getUniqueId(),
            tmp[k].c,
            tmp[k].beta);
      }
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

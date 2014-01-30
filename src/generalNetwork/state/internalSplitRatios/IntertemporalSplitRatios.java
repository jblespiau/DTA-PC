package generalNetwork.state.internalSplitRatios;

import java.util.HashMap;

import dataStructures.HashMapTripletDouble;
import dataStructures.Numerical;
import dataStructures.Triplet;
import generalLWRNetwork.Cell;
import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.Junction;
import generalLWRNetwork.Origin;
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
   */
  public void addNonCompliantSplitRatios(DiscretizedGraph g,
      JsonSplitRatios[] non_compliant_split_ratios,
      HashMap<Integer, Origin> node_to_origin) {
    assert g != null;
    if (non_compliant_split_ratios == null)
      return;
    assert node_to_origin != null;

    JsonJunctionSplitRatios[] tmp;
    int node_id;
    Origin orig;
    // For all the junctions
    for (int j = 0; j < non_compliant_split_ratios.length; j++) {
      node_id = non_compliant_split_ratios[j].node_id;
      IntertemporalJunctionSplitRatios ijsr =
          get(g.nodeToJunction(node_id).getUniqueId());

      tmp = non_compliant_split_ratios[j].split_ratios;
      // For all the entries
      for (int k = 0; k < tmp.length; k++) {
        // We check is the split ratio is from an origin
        orig = node_to_origin.get(node_id);
        if (orig != null) {
          ijsr.addNonCompliantSplitRatio(
              tmp[k].k,
              orig.getEntries()[0].getUniqueId(),
              g.firstCellofLink(tmp[k].out_id).getUniqueId(),
              tmp[k].c,
              tmp[k].beta);
        } else
          ijsr.addNonCompliantSplitRatio(
              tmp[k].k,
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

  /**
   * @brief Check that the sum at every junction of the non-compliant
   *        split-ratios is equal to 1.
   * @details The complexity is linear in the number of link (i, j) from one
   *          road to an other multiplied by the number of time steps.
   *          It can be improved since a lot of the split-ratios can be zero.
   * @param junctions
   *          The junctions of the network.
   * @return True is the network is valid, False otherwise.
   */
  public boolean check_data_integrity(Junction[] junctions) {
    Cell[] in, out;
    boolean is_valid = true;

    for (int j_id = 0; j_id < junctions.length; j_id++) {

      in = junctions[j_id].getPrev();
      out = junctions[j_id].getNext();

      if (out.length <= 1)
        /* We are facing a Nx1 junction which is automatically verified */
        continue;

      /*
       * We check the integrity of the non-compliant split ratios.
       * The integrity of the compliant split-ratios is verified by
       * construction since it is generated from the associated path.
       */
      double total = 0;
      HashMapTripletDouble[] non_compliant_split_ratios =
          get(j_id).non_compliant_split_ratios;

      for (int k = 0; k < non_compliant_split_ratios.length; k++) {
        for (int i = 0; i < in.length; i++) {
          for (int j = 0; j < out.length; j++) {
            Double result = non_compliant_split_ratios[k].
                get(new Triplet(in[i].getUniqueId(), out[j].getUniqueId(), 0));
            if (result != null) {
              total += result.doubleValue();
            }
          }
        }
        if (!Numerical.equals(total, 1.0, 0.001)) {
          System.out.println("[Warning] The sum of the non-compliant "
              + "split-ratios at junction " + j_id + " is not 1" +
              " (" + total + ")");
          is_valid = false;
        }
      }
    }
    return is_valid;
  }
}

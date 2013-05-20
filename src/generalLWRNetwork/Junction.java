package generalLWRNetwork;

import generalNetwork.state.CellInfo;
import generalNetwork.state.Profile;
import generalNetwork.state.splitRatios.JunctionSplitRatios;
import generalNetwork.state.splitRatios.Triplet;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @brief Represent a junctions between cells
 * @details For now we only accept 1*1 junctions
 * 
 */
public class Junction {

  private int unique_id;
  private Cell[] prev;
  private Cell[] next;

  public Junction() {
    unique_id = NetworkUIDFactory.getId_junctions();
    prev = null;
    next = null;
  }

  Junction(Cell[] predecessor, Cell[] successor) {
    unique_id = NetworkUIDFactory.getId_junctions();
    this.prev = predecessor.clone();
    this.next = successor.clone();
  }

  public int getUniqueId() {
    return unique_id;
  }

  public boolean isMergingJunction() {
    return next.length == 1;
  }

  public Cell[] getPrev() {
    return prev;
  }

  public void setPrev(Cell[] prev) {
    this.prev = prev.clone();
  }

  public Cell[] getNext() {
    return next;
  }

  public void setNext(Cell[] next) {
    this.next = next.clone();
  }

  public void addPrev(Cell c) {
    int i = 0;
    while (prev[i] != null)
      i++;
    prev[i] = c;
  }

  @Override
  public String toString() {
    String incells = "";
    for (int i = 0; i < prev.length; i++) {
      incells += prev[i].getUniqueId() + ",";
    }
    incells = "[" + incells + "]";

    String outcells = "";
    for (int i = 0; i < next.length; i++) {
      outcells += next[i].getUniqueId() + ",";
    }
    outcells = "[" + outcells + "]";
    return "[(" + unique_id + ")" + incells + "->" + outcells + "]";
  }

  public void checkConstraints() {
    assert (prev.length <= 1) : "Only junctions with less than 1 incoming link is working";
    assert (next.length <= 1) : "Only junctions with less than 1 outgoing link is working";
  }

  public void print() {
    System.out.println(toString());
  }

  public void solveJunction(Profile p, int time_step,
      JunctionSplitRatios junction_sr, Cell[] cells) {

    double flow;
    // 1x1 Junctions
    if (prev.length == 1 && next.length == 1) {
      CellInfo previous_info = p.get(prev[0]);
      CellInfo next_info = p.get(next[0]);
      flow = Math.min(next_info.supply, previous_info.demand);
      if (unique_id == 0) {
        System.out.println("Flow in Junction 0 at time step " + time_step
            + ": " + flow);
        previous_info.updateOutFlows(flow);
        System.out.println("Previous cellInfo ");
        previous_info.print();
      }
      next_info.updateInFlows(previous_info.out_flows, next[0].isSink());
    } else if (prev.length == 1) {

      /*
       * i = prev[0] is the incoming cell and j an outgoing cell at
       * the studied junction
       * We first compute for all flow_out_(i=0, k).
       * Then we compute flow_out (i,c,k) and flow_out(j,c,k)
       * If it is not zero we save it in the corresponding cells
       */
      /* We have: flow_out(i,j) = min (supply_j / beta(i,j), demand(i) */

      /* This saves the beta(i, j, c) */
      LinkedHashMap<Integer, Double> beta_i_j =
          new LinkedHashMap<Integer, Double>(next.length);

      CellInfo cell_i = p.get(prev[0]);

      double flow_out = cell_i.demand;

      /* If there is no no demand, there is no flow_in and out */
      if (flow_out == 0)
        return;

      // JunctionSplitRatios junction_sr =
      // internal_split_ratios.get(time_step, unique_id);
      Iterator<Entry<Triplet, Double>> iterator =
          junction_sr.compliant_split_ratios
              .entrySet()
              .iterator();
      Entry<Triplet, Double> entry;
      Triplet triplet;
      Double beta_ijc, previous_beta;
      Double density_ic;
      Integer out_id;
      /* Calculation of the beta(i, j) */
      while (iterator.hasNext()) {
        entry = iterator.next();
        triplet = entry.getKey();
        beta_ijc = entry.getValue();

        density_ic = cell_i.partial_densities.get(triplet.commodity);
        if (density_ic == null)
          continue;

        out_id = new Integer(triplet.outgoing);
        previous_beta = beta_i_j.get(out_id);
        if (previous_beta == null)
          beta_i_j.put(out_id, density_ic * beta_ijc);
        else
          beta_i_j.put(out_id, previous_beta + density_ic * beta_ijc);
      }

      Iterator<Entry<Integer, Double>> iterator_beta = beta_i_j
          .entrySet()
          .iterator();
      Entry<Integer, Double> beta_entry;
      double density_i = cell_i.total_density;
      assert density_i > 0;
      double beta_ij_dividedby_density;

      /* We compute flow_out(i,k) */
      while (iterator_beta.hasNext()) {
        beta_entry = iterator_beta.next();
        beta_ij_dividedby_density = beta_entry.getValue() / density_i;
        assert beta_entry.getValue() > 0;
        beta_i_j.put(beta_entry.getKey(), beta_ij_dividedby_density);
        assert beta_ij_dividedby_density > 0;

        flow_out = Math.min(flow_out,
            p.get(cells[beta_entry.getKey()]).supply
                / beta_ij_dividedby_density);
      }

      Iterator<Entry<Integer, Double>> iterator_partial_densities =
          cell_i.partial_densities.entrySet().iterator();
      Entry<Integer, Double> entry_density;
      double flow_out_dividedby_density = flow_out / density_i;
      double out_flow_for_commodity;
      while (iterator_partial_densities.hasNext()) {
        entry_density = iterator_partial_densities.next();

        /* We compute flow_out(i,c,k) */
        out_flow_for_commodity = flow_out_dividedby_density
            * entry_density.getValue();
        cell_i.out_flows.put(entry_density.getKey(), out_flow_for_commodity);

        for (int out = 0; out < next.length; out++) {
          /* We compute flow_in(j,c,k) */
          beta_ijc = junction_sr.get(prev[0].getUniqueId(),
              next[out].getUniqueId(),
              entry_density.getKey());
          if (beta_ijc == null)
            continue;
          else {
            p.get(next[out]).in_flows.put(entry_density.getKey(),
                beta_ijc * out_flow_for_commodity);
          }
        }

      }

    } else {
      System.out.println("Only 1x1 and 1xN junctions are working for now");
      System.exit(1);
    }
  }

}
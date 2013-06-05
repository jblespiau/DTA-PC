package generalLWRNetwork;

import generalNetwork.state.CellInfo;
import generalNetwork.state.JunctionInfo;
import generalNetwork.state.Profile;
import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.PairCells;
import dataStructures.Triplet;

/**
 * @brief Represent a junctions between cells
 * @details For now we only accept 1*1 junctions
 * 
 */
public class Junction {

  private int unique_id;
  private Cell[] prev;
  private Cell[] next;
  private HashMap<Integer, Double> priorities;

  public Junction() {
    unique_id = NetworkUIDFactory.getId_junctions();
    prev = null;
    next = null;
  }

  /**
   * @brief Create a junction that does NOT need any priority vector
   * @param predecessor
   * @param successor
   */
  public Junction(Cell[] predecessor, Cell[] successor) {
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

  public void setPriorities(HashMap<Integer, Double> priorities) {
    this.priorities = priorities;
    // TODO: Check priorities of sum 1
  }

  public double getPriority(int cell_id) {
    Double res = priorities.get(cell_id);
    if (res == null) {
      System.out.println("[Junction] This case should NEVER happen. Error");
      System.exit(1);
      return 0.0;
    }
    else
      return res.doubleValue();
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
    if (prev == null) {
      incells = "null";
    } else {
      for (int i = 0; i < prev.length; i++) {
        incells += prev[i].getUniqueId() + ",";
      }
    }
    incells = "[" + incells + "]";

    String outcells = "";
    if (next == null) {
      outcells = "null";
    } else {
      for (int i = 0; i < next.length; i++) {
        outcells += next[i].getUniqueId() + ",";
      }
    }
    outcells = "[" + outcells + "]";

    if (priorities == null)
      return "[(" + unique_id + ")" + incells + "->" + outcells + "]";
    else
      return "[(" + unique_id + ")" + incells + "->" + outcells + "("
          + priorities.toString() + "]";
  }

  public void print() {
    System.out.println(toString());
  }

  /**
   * @brief Solve the flows at the junction
   * @param p
   * @param time_step
   * @param junction_sr
   * @param cells
   *          TODO: Remove time_step
   */
  public void solveJunction(Profile p, int time_step,
      JunctionSplitRatios junction_sr, Cell[] cells) {

    /* We create the junction info */
    JunctionInfo j_info = new JunctionInfo(prev.length, next.length);
    p.putJunction(unique_id, j_info);

    double flow;
    // 1x1 Junctions
    if (prev.length == 1 && next.length == 1) {
      CellInfo previous_info = p.getCell(prev[0]);
      CellInfo next_info = p.getCell(next[0]);

      j_info.putAggregateSR(prev[0], next[0], 1.0);

      flow = Math.min(next_info.supply, previous_info.demand);
      /*
       * For debug
       * 
       * System.out.println("Flow in Junction 1 at time step " + time_step
       * + ": " + flow);
       * System.out.println("Previous cellInfo ");
       * previous_info.print();
       * }
       */
      previous_info.updateOutFlows(flow);
      next_info.updateInFlows(previous_info.out_flows, next[0].isSink());

      // 2x1 junctions
    } else if (prev.length == 2 && next.length == 1) {

      CellInfo prev1 = p.getCell(prev[0]);
      CellInfo prev2 = p.getCell(prev[1]);
      CellInfo next_info = p.getCell(next[0]);

      j_info.putAggregateSR(prev[0], next[0], 1.0);
      j_info.putAggregateSR(prev[1], next[0], 1.0);

      double demand1 = prev1.demand;
      double demand2 = prev2.demand;
      flow = Math.min(demand1 + demand2, next_info.supply);
      if (flow == 0)
        return;

      Double P1 = priorities.get(prev[0].getUniqueId());
      Double P2 = priorities.get(prev[1].getUniqueId());
      assert P1 != null && P2 != null : "In 2x1 solving, we didn't found the priority for both roads";

      Double flow_1, flow_2;
      if (P1 * (flow - demand1) > P2 * demand1) {
        flow_1 = demand1;
      } else if (P1 * demand2 < P2 * (flow - demand2)) {
        flow_1 = flow - demand2;
      } else {
        flow_1 = P1 / (P1 + P2) * flow;
      }
      flow_2 = flow - flow_1;

      assert flow_1 <= demand1;
      assert flow_2 <= demand2;
      /* Computing the partial out-flow for the first incoming link */
      if (flow_1 != 0) {
        Iterator<Entry<Integer, Double>> iterator_partial_densities =
            prev1.partial_densities.entrySet().iterator();
        Entry<Integer, Double> entry_density;
        double flow_out_dividedby_density = flow_1 / prev1.total_density;
        double out_flow_for_commodity;
        while (iterator_partial_densities.hasNext()) {
          entry_density = iterator_partial_densities.next();

          /* We compute flow_out(1,c,k) */
          out_flow_for_commodity = flow_out_dividedby_density
              * entry_density.getValue();
          prev1.out_flows.put(entry_density.getKey(), out_flow_for_commodity);

          /* We add it into the in-flow of the next */
          Double in_flow = next_info.in_flows.get(entry_density.getKey());

          if (in_flow == null) {
            in_flow = 0.0;
          }
          next_info.in_flows.put(entry_density.getKey(), in_flow
              + out_flow_for_commodity);
        }
      }

      /* Computing the partial out-flow for the second incoming link */
      if (flow_2 != 0) {
        Iterator<Entry<Integer, Double>> iterator_partial_densities =
            prev2.partial_densities.entrySet().iterator();
        Entry<Integer, Double> entry_density;
        double flow_out_dividedby_density = flow_2 / prev2.total_density;
        double out_flow_for_commodity;
        while (iterator_partial_densities.hasNext()) {
          entry_density = iterator_partial_densities.next();

          /* We compute flow_out(1,c,k) */
          out_flow_for_commodity = flow_out_dividedby_density
              * entry_density.getValue();
          prev2.out_flows.put(entry_density.getKey(), out_flow_for_commodity);

          /* We add it into the in-flow of the next */
          Double in_flow = next_info.in_flows.get(entry_density.getKey());

          if (in_flow == null) {
            in_flow = 0.0;
          }
          next_info.in_flows.put(entry_density.getKey(), in_flow
              + out_flow_for_commodity);
        }
      }
      // 1xN junctions
    } else if (prev.length == 1) {

      /*
       * in_id = prev[0] is the incoming cell and j an outgoing cell at
       * the studied junction
       * We first compute for all flow_out_(in_id, k).
       * Then we compute flow_out (in_id,c,k) and flow_in(j,c,k)
       * If it is not zero we save it in the corresponding cells
       */
      /*
       * We have: flow_out(in_id,j) = min (supply_j / beta(in_id,j),
       * demand(in_id)
       */

      /* JunctionInfo j_info is used to saves the beta(in_id, j, c) */
      int in_id = prev[0].getUniqueId();

      CellInfo cell_i = p.getCell(prev[0]);

      double flow_out = cell_i.demand;

      /* If there is no no demand, there is no flow-out and in */
      if (flow_out == 0)
        return;

      Iterator<Entry<Triplet, Double>> iterator =
          junction_sr.compliant_split_ratios
              .entrySet()
              .iterator();
      Entry<Triplet, Double> entry;
      Triplet triplet;
      Double beta_ijc, previous_beta;
      Double density_ic;
      Integer out_id;
      /* Calculation of the beta(i, j) * density(i,k) */
      while (iterator.hasNext()) {
        entry = iterator.next();
        triplet = entry.getKey();
        beta_ijc = entry.getValue();

        density_ic = cell_i.partial_densities.get(triplet.commodity);
        if (density_ic == null)
          continue;

        out_id = new Integer(triplet.outgoing);
        assert out_id != null;
        assert triplet.incoming == in_id;

        previous_beta = j_info.getAggregateSR(in_id, out_id.intValue());
        if (previous_beta == null)
          j_info
              .putAggregateSR(in_id, out_id.intValue(), density_ic * beta_ijc);
        else
          j_info.putAggregateSR(in_id, out_id.intValue(), previous_beta
              + density_ic * beta_ijc);
      }

      Iterator<Entry<PairCells, Double>> iterator_beta = j_info.entryIterator();

      Entry<PairCells, Double> beta_entry;
      PairCells i_j;
      double density_i = cell_i.total_density;
      assert density_i > 0;
      double beta_ij_dividedby_density;

      /* We compute flow_out(in_id,k) */
      while (iterator_beta.hasNext()) {
        beta_entry = iterator_beta.next();
        beta_ij_dividedby_density = beta_entry.getValue() / density_i;
        i_j = beta_entry.getKey();

        /* Computation of beta(i, j) by dividing by density(i,k) */
        j_info.put(i_j, beta_ij_dividedby_density);

        assert i_j.incoming == in_id;
        assert beta_entry.getValue() >= 0 : "Negative value in Junction (" + beta_entry.getValue() + ")";
        assert beta_ij_dividedby_density >= 0;

        flow_out = Math.min(flow_out,
            p.getCell(cells[i_j.outgoing]).supply
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
            p.getCell(next[out]).in_flows.put(entry_density.getKey(),
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
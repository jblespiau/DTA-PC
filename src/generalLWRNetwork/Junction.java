package generalLWRNetwork;

import generalNetwork.state.CellInfo;
import generalNetwork.state.JunctionInfo;
import generalNetwork.state.Profile;
import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dataStructures.PairCells;

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
   * @details It must also fill in the junction info with the flow, the supply
   *          or demand constrained state and the aggregate split ratios
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

    // 1x1 Junctions
    if (prev.length == 1 && next.length == 1) {
      CellInfo previous_info = p.getCell(prev[0]);
      CellInfo next_info = p.getCell(next[0]);
      double flow;

      if (next_info.supply < previous_info.demand) {
        flow = next_info.supply;
        j_info.set_supply_limited(next[0].getUniqueId());
      } else if (next_info.supply > previous_info.demand) {
        flow = previous_info.demand;
        j_info.set_demand_limited();
      } else {
        flow = previous_info.demand;
      }

      j_info.putAggregateSR(prev[0], next[0], 1.0);
      j_info.putFlowOut(prev[0], flow);

      previous_info.updateOutFlows(flow);
      next_info.updateInFlows(previous_info.out_flows, next[0].isSink());

      // 1xN junctions
    } else if (prev.length == 1) {

      /* JunctionInfo j_info is used to saves the beta(in_id, j, c) */
      /* in_id is the id of the single incoming link */
      int in_id = prev[0].getUniqueId();
      CellInfo cell_i = p.getCell(prev[0]);

      /*
       * We first compute flow_out_(in_id, k) =
       * min ({supply_j / beta(in_id,j) when beta(in_id,j) > 0}, demand(in_id)
       * Then we compute flow_out (in_id,c,k) and flow_in(j,c,k)
       */

      double demand = cell_i.demand;

      /* If there is no no demand, there is no flow-out and in */
      if (demand == 0)
        return;

      /*
       * Computation of kapa =
       * sum[beta(i, j, c)(k) * density(i,c,k)]
       */
      Iterator<Entry<Integer, Double>> iterator_partial_densities =
          cell_i.partial_densities.entrySet().iterator();
      Entry<Integer, Double> entry_density;
      Integer commodity;
      Double partial_density, kapa, beta_ijc;
      int out_id;
      while (iterator_partial_densities.hasNext()) {
        entry_density = iterator_partial_densities.next();
        commodity = entry_density.getKey();
        partial_density = entry_density.getValue();

        assert (partial_density != null);
        if (partial_density == 0)
          continue;

        for (int out = 0; out < next.length; out++) {
          out_id = next[out].getUniqueId();
          beta_ijc = junction_sr.get(in_id, out_id, commodity);
          if (beta_ijc == null)
            continue;

          kapa = j_info.getAggregateSR(in_id, out_id);
          if (kapa == null)
            j_info.putAggregateSR(in_id, out_id, partial_density * beta_ijc);
          else
            j_info.putAggregateSR(in_id, out_id, kapa + partial_density
                * beta_ijc);
        }
      }

      /*
       * Then we get the real beta(i,j) = 1/ total_density * previous thing
       * At the same time we compute the flow-out
       */
      Iterator<Entry<PairCells, Double>> iterator_beta = j_info.entryIterator();
      Entry<PairCells, Double> beta_entry;
      PairCells i_j;
      double density_i = cell_i.total_density;
      assert density_i > 0;
      double beta_ij_dividedby_density;

      /* We compute flow_out(in_id,k) */
      double flow_out = demand;
      boolean is_single_minimum = true;
      int limiting_supply = -1;
      while (iterator_beta.hasNext()) {
        beta_entry = iterator_beta.next();
        i_j = beta_entry.getKey();
        /* Computation of beta(i, j) by dividing by density(i,k) */
        beta_ij_dividedby_density = beta_entry.getValue() / density_i;

        j_info.put(i_j, beta_ij_dividedby_density);

        assert i_j.incoming == in_id;
        assert beta_entry.getValue() >= 0 : "Negative value in Junction ("
            + beta_entry.getValue() + ")";
        assert beta_ij_dividedby_density >= 0;

        double supply = p.getCell(cells[i_j.outgoing]).supply
            / beta_ij_dividedby_density;

        if (flow_out < supply) {
          /* In this case this supply is not limiting the flow */
        } else if (flow_out > supply) {
          /* In this case, this supply is limiting the flow */
          is_single_minimum = true;
          limiting_supply = i_j.outgoing;
          flow_out = supply;
        } else {
          /*
           * In that case, 2 supplies or the demand and one supply are both
           * limiting the flow
           */
          is_single_minimum = false;
        }
      }

      /* We determine if the junction is supply or demand limited */
      if (is_single_minimum) {
        if (flow_out == demand)
          j_info.set_demand_limited();
        else {
          assert limiting_supply != -1;
          j_info.set_supply_limited(limiting_supply);
        }
      }
      /*
       * We register the total out-flow at the junction (easy because only one
       * incomming linkg
       */
      j_info.putFlowOut(in_id, flow_out);

      /* Then we compute the partial flow-out and flow int */
      iterator_partial_densities =
          cell_i.partial_densities.entrySet().iterator();
      double flow_out_dividedby_density = flow_out / density_i;
      double out_flow_for_commodity;
      while (iterator_partial_densities.hasNext()) {
        entry_density = iterator_partial_densities.next();
        commodity = entry_density.getKey();

        /* We compute flow_out(i,c,k) */
        out_flow_for_commodity = flow_out_dividedby_density
            * entry_density.getValue();
        cell_i.out_flows.put(entry_density.getKey(), out_flow_for_commodity);

        for (int out = 0; out < next.length; out++) {
          /* We compute flow_in(j,c,k) */
          beta_ijc = junction_sr.get(in_id,
              next[out].getUniqueId(),
              commodity);
          if (beta_ijc == null)
            continue;
          else {
            p.getCell(next[out]).in_flows.put(commodity,
                beta_ijc * out_flow_for_commodity);
          }
        }
      }
      // 2x1 junctions
    } else if (prev.length == 2 && next.length == 1) {

      CellInfo prev1 = p.getCell(prev[0]);
      CellInfo prev2 = p.getCell(prev[1]);
      CellInfo next_info = p.getCell(next[0]);

      j_info.putAggregateSR(prev[0], next[0], 1.0);
      j_info.putAggregateSR(prev[1], next[0], 1.0);

      double demand1 = prev1.demand;
      double demand2 = prev2.demand;
      double flow;
      /* We determine if the junction is supply or demand limited */
      if (demand1 + demand2 < next_info.supply) {
        flow = demand1 + demand2;
        j_info.set_demand_limited();
      } else if (demand1 + demand2 > next_info.supply) {
        j_info.set_supply_limited(next[0].getUniqueId());
        flow = next_info.supply;
      } else
        flow = next_info.supply;

      if (flow == 0)
        return;

      Double P1 = priorities.get(prev[0].getUniqueId());
      Double P2 = priorities.get(prev[1].getUniqueId());
      assert P1 != null && P2 != null : "In 2x1 solving, we didn't found the priority for both roads";

      double flow_1, flow_2;
      if (P1 * (flow - demand1) > P2 * demand1) {
        flow_1 = demand1;
      } else if (P1 * demand2 < P2 * (flow - demand2)) {
        flow_1 = flow - demand2;
      } else {
        flow_1 = P1 / (P1 + P2) * flow;
      }
      flow_2 = flow - flow_1;

      //TODO: define supply and demand limitied out of the physical set
      /* We register the total out-flow at the junction */
      j_info.putFlowOut(prev[0], flow_1);
      j_info.putFlowOut(prev[1], flow_2);

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
    } else {
      System.out.println("Only 1x1 and 1xN junctions are working for now");
      System.exit(1);
    }
  }
}
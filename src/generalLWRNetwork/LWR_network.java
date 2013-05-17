package generalLWRNetwork;

import generalNetwork.state.CellInfo;
import generalNetwork.state.Profile;
import generalNetwork.state.splitRatios.HashMapPairDouble;
import generalNetwork.state.splitRatios.IntertemporalSplitRatios;
import generalNetwork.state.splitRatios.JunctionSplitRatios;
import generalNetwork.state.splitRatios.Triplet;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

public class LWR_network {

  private Cell[] cells;
  private Junction[] junctions;
  private Origin[] sources;
  private Destination[] sinks;

  private IntertemporalSplitRatios internal_split_ratios;
  private int nb_commodities;

  /**
   * @brief Take a DiscretizedGraph and create the LWR_network compact
   *        representation of it.
   * @details It does not create any cells but uses the ones in the graph
   */
  public LWR_network(DiscretizedGraph g) {
    int total_nb_junctions = g.total_nb_junctions;

    if (g.junctions.length + g.new_junctions.size() != total_nb_junctions) {
      System.out.println("[LWR_network]Different number of junctions: "
          + (g.junctions.length + g.new_junctions.size())
          + " vs " + total_nb_junctions);
      System.exit(1);
    }

    int total_nb_cells = g.total_nb_cells;
    if (total_nb_cells != g.new_cells.size()) {
      System.out.println("Different number of cells: " + total_nb_cells
          + " vs "
          + g.new_cells.size());
      System.exit(1);
    }

    /* We register all the cells */
    cells = new Cell[total_nb_cells];
    ListIterator<Cell> iterator = g.new_cells.listIterator();
    Cell tmp;
    while (iterator.hasNext()) {
      tmp = iterator.next();
      cells[tmp.getUniqueId()] = tmp;
    }

    /* We register all the junctions */
    junctions = new Junction[total_nb_junctions];
    ListIterator<Junction> iterator2 = g.new_junctions.listIterator();
    Junction tmp2;
    while (iterator2.hasNext()) {
      tmp2 = iterator2.next();
      junctions[tmp2.getUniqueId()] = tmp2;
    }
    for (int i = 0; i < g.junctions.length; i++) {
      junctions[g.junctions[i].getUniqueId()] = g.junctions[i];
    }

    check();

    sources = g.sources.clone();
    sinks = g.destinations.clone();

    internal_split_ratios = g.split_ratios;
    nb_commodities = g.nb_paths;
  }

  private void check() {
    /* We check that all registered cells and junctions are not null */
    for (int i = 0; i < cells.length; i++) {
      assert cells[i] != null : "Null cell found !";
    }
    /* We also check that all junctions have an outgoing cell */
    for (int i = 0; i < junctions.length; i++) {
      assert junctions[i] != null : "Null junction found !";
      assert junctions[i].getNext() != null;
      assert junctions[i].getNext().length > 0 : "A junction has no outgoing cell !";
    }
  }

  public void print() {

    for (int c = 0; c < cells.length; c++)
      cells[c].print();
    for (int j = 0; j < junctions.length; j++)
      junctions[j].print();

  }

  public void printInternalSplitRatios() {
    System.out.println("Printing the internal split ratios:");
    System.out.println(internal_split_ratios.toString());
  }

  public int getNb_Cells() {
    return cells.length;
  }

  public int getNb_Junctions() {
    return junctions.length;
  }

  public void addCell(Cell c) {
    cells[c.getUniqueId()] = c;
  }

  public Cell getCell(int i) {
    return cells[i];
  }

  public Destination getSink(int path) {
    return sinks[path];
  }

  public void addJunction(Junction j) {
    junctions[j.getUniqueId()] = j;
  }

  public Junction getJunction(int i) {
    return junctions[i];
  }

  public Junction[] getJunctions() {
    return junctions;
  }

  public int getNumber_paths() {
    int result = 0;
    for (int i = 0; i < sources.length; i++) {
      result += sources[i].size();
    }
    return result;
  }

  public void checkConstraints(double delta_t) {
    for (int c = 0; c < cells.length; c++)
      cells[c].checkConstraints(delta_t);

    for (int j = 0; j < junctions.length; j++)
      junctions[j].checkConstraints();
  }

  /**
   * @brief Add flow in a given cell_info representing a buffer
   * @param previous_densities
   *          This is NOT modified
   * @param demands
   *          The demand we want to add in the buffer
   * @param cell_info
   *          The information of the buffer
   */
  private void injectDemand(
      LinkedHashMap<Integer, Double> previous_densities,
      LinkedHashMap<Integer, Double> demands, CellInfo cell_info) {

    LinkedHashMap<Integer, Double> new_densities = new LinkedHashMap<Integer, Double>(
        previous_densities.size());

    Iterator<Entry<Integer, Double>> demand_iterator = demands.entrySet()
        .iterator();
    Entry<Integer, Double> pair;
    Double previous_density;
    double total_density = 0;
    while (demand_iterator.hasNext()) {
      pair = demand_iterator.next();
      previous_density = previous_densities.get(pair.getKey());

      if (previous_density == null) {
        total_density += pair.getValue();
        new_densities.put(pair.getKey(), pair.getValue());
      } else {
        total_density += previous_density + pair.getValue();
        new_densities.put(pair.getKey(),
            previous_density + pair.getValue());
      }
    }
    cell_info.total_density = total_density;
    cell_info.partial_densities = new_densities;
  }

  /**
   * @details - Add the demand in the buffer of the previous profile to get
   *          the buffer of the profile p
   *          - Computes the demand and supply of profile p
   *          - Computes the in- and out-flows for profile p
   *          - Create a new profile containing the new densities
   * @param previous_profile
   *          This is NOT modified
   * @param p
   *          Only the buffer of this profile are modified
   * @param demands
   *          demands[i] is the demand {(commodity, value)} we have to put
   *          in entries[i]
   * 
   */
  /* Works for only one source, one destination */
  public Profile simulateProfileFrom(Profile previous_profile, Profile p,
      double delta_t, Double origin_demand, HashMapPairDouble splits,
      int time_step) {
    assert p.size() == cells.length : "The profile size must correspond to the size of the network";
    // assert demands.length == sources.length :
    // " The demands should correspond to the number of entries";

    Profile next_profile = new Profile(cells.length);

    /* We inject the demand in the buffers of the profile p */
    for (int b = 0; b < sources.length; b++) {
      sources[b].injectDemand(previous_profile, p, origin_demand, splits);
    }

    /* Computation of the demand and supply */
    double density, demand, supply;
    for (int cell_id = 0; cell_id < cells.length; cell_id++) {
      density = p.get(cell_id).total_density;

      /*
       * The demand and the supply depend on the network and the density
       */
      demand = getCell(cell_id).getDemand(density);
      supply = getCell(cell_id).getSupply(density);
      assert demand >= 0 : "Demand should be positive";
      assert supply >= 0 : "Supply should be positive";

      p.get(cell_id).demand = demand;
      p.get(cell_id).supply = supply;

      // We clear the old flows
      p.get(cell_id).clearFlow();
    }

    /* Computation of the flows */
    // TODO: Put this in the junctions
    double flow;
    Junction j;
    Cell[] previous_cells, next_cells;
    for (int j_id = 0; j_id < junctions.length; j_id++) {

      j = getJunction(j_id);
      previous_cells = j.getPrev();
      next_cells = j.getNext();

      // 1x1 Junctions
      if (previous_cells.length == 1 && next_cells.length == 1) {
        CellInfo previous = p.get(j.getPrev()[0]);
        CellInfo next = p.get(j.getNext()[0]);
        flow = Math.min(next.supply, previous.demand);
        if (j_id == 0) {
          System.out.println("Flow in Junction 0 at time step " + time_step
              + ": " + flow);
          previous.updateOutFlows(flow);
          System.out.println("Previous cellInfo ");
          previous.print();
        }
        next.updateInFlows(flow);
      } else if (previous_cells.length == 1) {

        /*
         * i = previous_cells[0] is the incoming cell and j an outgoing cell at
         * the studied junction
         * We first compute for all flow_out_(i=0, k).
         * Then we compute flow_out (i,c,k) and flow_out(j,c,k)
         * If it is not zero we save it in the corresponding cells
         */
        /* We have: flow_out(i,j) = min (supply_j / beta(i,j), demand(i) */

        /* This saves the beta(i, j, c) */
        LinkedHashMap<Integer, Double> beta_i_j =
            new LinkedHashMap<Integer, Double>(next_cells.length);

        CellInfo cell_i = p.get(previous_cells[0]);

        double flow_out = cell_i.demand;

        /* If there is no no demand, there is no flow_in and out */
        if (flow_out == 0)
          continue;

        JunctionSplitRatios junction_sr =
            internal_split_ratios.get(time_step, j_id);
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

          for (int out = 0; out < next_cells.length; out++) {
            /* We compute flow_in(j,c,k) */
            beta_ijc = junction_sr.get(previous_cells[0].getUniqueId(),
                next_cells[out].getUniqueId(),
                entry_density.getKey());
            if (beta_ijc == null)
              continue;
            else {
              p.get(next_cells[out]).in_flows.put(entry_density.getKey(),
                  beta_ijc * out_flow_for_commodity);
            }
          }

        }

      } else {
        System.out.println("Only 1x1 and 1xN junctions are working for now");
        System.exit(1);
      }
    }

    /* Creation of the new profile with the new densities */
    LinkedHashMap<Integer, Double> new_densities, densities, in_flows, out_flows;
    CellInfo cell_info;
    for (int cell_id = 0; cell_id < cells.length; cell_id++) {
      cell_info = p.get(cell_id);
      densities = cell_info.partial_densities;

      in_flows = cell_info.in_flows;
      out_flows = cell_info.out_flows;

      new_densities = getCell(cell_id).getUpdatedDensity(densities,
          in_flows, out_flows, delta_t);

      next_profile.put(cell_id, new CellInfo(new_densities));
    }

    return next_profile;
  }

  public Profile emptyProfile() {
    Profile initial_profile = new Profile(cells.length);
    for (int cell_id = 0; cell_id < cells.length; cell_id++) {
      initial_profile.put(cell_id, new CellInfo());
    }
    return initial_profile;
  }

  public double[] initialSplitRatios() {
    double[] res = new double[sources.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = 1.0 / (double) res.length;
    }
    return res;
  }

}
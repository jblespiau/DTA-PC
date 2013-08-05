package dta_solver;

import io.InputOutput;

import java.util.Iterator;

import generalLWRNetwork.Cell;
import generalLWRNetwork.Destination;
import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalLWRNetwork.Origin;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.Demands;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.Graph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.Profile;
import generalNetwork.state.State;
import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;

/**
 * @package dta_solver
 * @brief High level components related Dynamic Traffic Assignment
 */
/**
 * @class Simulator
 * @brief Represents a simulatr for a DTA problem (both graphs and conditions)
 */
public class Simulator {

  /** The discretized version of the graph. Should not be used */
  public DiscretizedGraph discretized_graph;
  /** The compact form of the graph */
  public LWR_network lwr_network;
  /** The time discretization (delta t and number of time steps) */
  public Discretization time_discretization;
  /** The value of the demand at the origin for every time steps */
  public Demands origin_demands;
  /** The split ratios at the origins (compliant and non compliant agents) */
  public IntertemporalOriginsSplitRatios splits;

  /** Share of the compliant flow */
  private double alpha;

  protected Simulator(int delta_t, int nb_steps) {
    time_discretization = new Discretization(delta_t, nb_steps);
  }

  /**
   * @brief Creates a Dynamic Traffic Assignment with Partial Compliance
   *        simulator
   * @param network_file
   *          File containing the description of the network
   * @param data_file
   *          File containing the demands and non-compliant split ratios
   * @param alpha
   *          Share of the compliant-flow
   * @param debug
   *          True will enable debug mode and debug printing
   */
  public Simulator(
      String network_file,
      String data_file,
      double alpha,
      boolean debug) {
    /* For now we study full compliance. TODO: modify this */
    this.alpha = alpha;

    JsonFactory json = new JsonFactory(debug);

    /* Loading the graph description */
    System.out.print("Loading the graph from JSON...");
    Graph json_graph = json.graphFromFile(network_file);
    System.out.println("Done");

    /* Loading the data from the json file */
    System.out.print("Loading the data from JSON...");
    Json_data data = json.dataFromFile(data_file);
    System.out.println("Done");

    /* Time discretization from the data */
    time_discretization = new Discretization(data.delta_t, data.max_time_step);

    int nb_steps = time_discretization.getNb_steps();
    double delta_t = time_discretization.getDelta_t();
    /* Creation of the network */
    System.out.print("Discretization of the graph...");
    discretized_graph = new DiscretizedGraph(json_graph, delta_t, nb_steps);
    System.out.println("Done");

    /* Creation of the demands */
    System.out.print("Loading demands from JSON...");
    origin_demands = new DemandsFactory(time_discretization,
        delta_t, data.demands, discretized_graph.node_to_origin)
        .buildDemands();
    System.out.println("Done");

    if (debug)
      System.out.println(origin_demands.toString());

    /* Creation of the non-compliant split-ratios */
    if (alpha != 1) {
      System.out.print("Loading non compliant split-ratios from JSON...");
      discretized_graph.split_ratios.addNonCompliantSplitRatios(
          discretized_graph,
          data.non_compliant_split_ratios,
          discretized_graph.node_to_origin);
      System.out.println("Done");
    } else {
      System.out.println("Full System Optimal detected.");
    }

    /*
     * Initialization of a physical set for the control split-ratios at the
     * origins
     */
    initializSplitRatios();

    if (debug)
      System.out.println(splits.toString());

    /* Building compact form in LWR_network */
    System.out.print("Creating the compact representation...");
    lwr_network = new LWR_network(discretized_graph);
    System.out.println("Done");

    /* Checking the requirements on the network */
    System.out
        .print("Checking that the network respect needed requirements...");
    lwr_network.checkConstraints(delta_t);
    System.out.println("Done");

    if (debug) {
      System.out.println("Printing the compact form");
      lwr_network.print();
      lwr_network.printInternalSplitRatios();
    }
  }

  protected State run() {
    return run(true);
  }

  /**
   * @brief Run the simulation and returns a partial state not including the sum
   *        of the split ratios at every origin for all time steps
   */
  public State partialRun(boolean print) {
    return run(print);
  }

  /**
   * @brief Run the simulation and returns the state without verbose printing
   * @return The state profile after the simulation
   */
  public State partialRun() {
    return run(false);
  }

  protected State run(boolean print) {
    int T = time_discretization.getNb_steps();
    double delta_t = time_discretization.getDelta_t();
    Profile[] profiles = new Profile[T];

    for (int k = 0; k < T; k++) {
      if (k == 0) {
        profiles[k] = lwr_network.emptyProfile();
      } else if (k == 1) {
        profiles[k] = lwr_network.simulateProfileFrom(
            lwr_network.emptyProfile(),
            profiles[k - 1],
            delta_t,
            origin_demands, splits,
            k - 1);
      } else {
        profiles[k] = lwr_network.simulateProfileFrom(profiles[k - 2],
            profiles[k - 1],
            delta_t,
            origin_demands, splits,
            k - 1);
      }

      if (k > 0 && print) {
        System.out.println("****** Printing profile at time step " + (k - 1)
            + "********");
        profiles[k - 1].print();
      }
    }
    /*
     * We run the simulation for the last time step to update what is needed
     * (junctionInfo)
     */
    assert T >= 2;
    lwr_network.simulateProfileFrom(profiles[T - 2],
        profiles[T - 1],
        delta_t,
        origin_demands, splits,
        T - 1);

    if (print) {
      System.out.println("****** Printing profile at time step " + (T - 1)
          + "********");
      profiles[T - 1].print();
    }

    /* Check that all the JunctionInfo are not null */
    int J = lwr_network.getNb_Junctions();
    for (int k = 0; k < T; k++)
      for (int j = 0; j < J; j++)
        assert profiles[k].getJunction(j) != null : "Null JunctionInfo for time step "
            + k + ", junction " + j;

    return new State(profiles);
  }

  /**
   * @return The share of the compliant agents
   */
  public double getAlpha() {
    return alpha;
  }

  /**
   * @return True if all agents are controlled. False otherwise.
   */
  public boolean isFullSystemOptimal() {
    return alpha == 1.0;
  }

  /**
   * @brief Initialization of a physical set for the control split-ratios at the
   *        origins
   */
  public void initializSplitRatios() {

    int nb_steps = time_discretization.getNb_steps();
    System.out
        .print("Initializing physical split-ratios at the origins...");
    splits =
        IntertemporalOriginsSplitRatios.defaultPhysicalSplitRatios(
            nb_steps,
            discretized_graph.sources, alpha);
    System.out.println("Done");
  }

  /**
   * @brief Initialize split-ratios that are valid one for any optimizer
   * @details An optimizer works with some split-ratios such that the sum of the
   *          split-ratios at every origin for every time steps is greater than
   *          1. It initialized the non-compliant split ratios to its real value
   */
  protected void initializeSplitRatiosForOptimizer() {

    assert alpha != 0 : "The share of the compliant commodities is zero. No optimization of the compliant commodities can be done, so initializing the split-ratios for an optimizer is illegal";
    int nb_steps = time_discretization.getNb_steps();

    /*
     * Initialization of a not physical set for the control split-ratios at the
     * origins
     */
    System.out
        .print("Initializing split-ratios for the optimizer at the origins...");
    splits =
        IntertemporalOriginsSplitRatios.defaultNotPhysicalSpitRatio(
            nb_steps,
            discretized_graph.sources, alpha);
    System.out.println("Done");
  }

  /**
   * @brief Return the 1x(C+O)*T= matrix representing the compliant and
   *        non-compliant commodities where C is the number of compliant
   *        commodities and 0 the number of origins
   * @details There are T blocks of size (C+O). The i-th block contains the
   *          controls at time step i.
   */
  private double[] getFullControl() {

    int T = time_discretization.getNb_steps();
    int C = lwr_network.getNb_compliantCommodities();
    Origin[] sources = lwr_network.getSources();
    int O = sources.length;
    int temporal_control_block_size = C;

    /* For every time steps there are C compliant flows, and O non compliant */
    double[] control = new double[T * (C + 1)];

    int index_in_control = 0;
    int commodity;
    Double split_ratio;
    for (int orig = 0; orig < O; orig++) {
      for (int k = 0; k < T; k++) {
        split_ratio = splits.get(sources[orig], k).get(0);
        if (split_ratio != null) {
          control[k * temporal_control_block_size + index_in_control] = split_ratio;
        }
      }
      index_in_control++;

      Iterator<Integer> it = sources[orig]
          .getCompliant_commodities()
          .iterator();
      while (it.hasNext()) {
        commodity = it.next();
        for (int k = 0; k < T; k++) {
          split_ratio = splits.get(sources[orig], k).get(commodity);
          if (split_ratio != null) {
            control[k * temporal_control_block_size + index_in_control] = split_ratio;
          }
        }
        index_in_control++;
      }
    }
    return control;
  }

  public void printFullControl() {
    int C = lwr_network.getNb_compliantCommodities();
    InputOutput.printControl(getFullControl(), C + 1);
  }

  /**
   * @details This function imposes that the control is physical (every split
   *          ratio is positive)
   */
  public double objective() {
    return objective(partialRun());
  }

  /**
   * @brief Computes the objective function:
   *        \sum_(i,c,k) \rho(i,c,k)
   *        - \sum_{origin o} epsilon2 * ln(\sum \rho(o,c,k) - 1)
   * @details
   *          The condition \beta >= 0 is already put in the solver (in
   *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
   *          in J
   */
  public double objective(State state) {
    double objective = 0;

    Cell[] cells = lwr_network.getCells();
    Destination[] destinations = lwr_network.getSinks();
    int T = time_discretization.getNb_steps();
    /*
     * To compute the sum of the densities ON the network, we add the density of
     * all the cells and then remove the density of the sinks
     */
    for (int k = 0; k < T; k++) {
      for (int cell_id = 0; cell_id < cells.length; cell_id++)
        objective += state.profiles[k].getCell(cell_id).total_density
            * cells[cell_id].getLength();

      for (int d = 0; d < destinations.length; d++)
        objective -= state.profiles[k].getCell(destinations[d].getUniqueId()).total_density
            * cells[destinations[d].getUniqueId()].getLength();
    }
    return objective;
  }
}
package dta_solver;

import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.Demands;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.Graph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.Profile;
import generalNetwork.state.State;
import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;

public class Simulator {

  DiscretizedGraph discretized_graph;
  public Discretization time_discretization;
  Demands origin_demands;
  public IntertemporalOriginsSplitRatios splits;
  public LWR_network lwr_network;
  /* Share of the compliant flow */
  private double alpha;

  protected Simulator(int delta_t, int nb_steps) {
    time_discretization = new Discretization(delta_t, nb_steps);
  }

  public Simulator(
      String network_file,
      String data_file,
      boolean debug) {

    alpha = 1.0;

    JsonFactory json = new JsonFactory(true);

    /* Loading the graph description */
    System.out.print("Loading the graph from JSON...");
    Graph json_graph = json.graphFromFile(network_file);
    System.out.println("Done");

    /* Loading the data from the json file */
    System.out.print("Loading the data from JSON...");
    Json_data data = json.dataFromFile(data_file);
    System.out.println("Done");

    /* Time discretization from the data */
    System.out.println("Max time: " + data.max_time_step);
    time_discretization = new Discretization(data.delta_t, data.max_time_step);

    /* Creation of the network */
    System.out.print("Discretization of the graph...");
    discretized_graph = new DiscretizedGraph(json_graph,
        time_discretization.getDelta_t(), time_discretization.getNb_steps());
    System.out.println("Done");

    System.out.print("Loading demands from JSON...");
    origin_demands = new DemandsFactory(time_discretization,
        data.delta_t, data.demands, discretized_graph.node_to_origin)
        .buildDemands();
    System.out.println("Done");

    if (debug)
      System.out.println(origin_demands.toString());

    /* Creation of the non-compliant split-ratios */
    System.out.print("Loading non compliant split-ratios from JSON...");
    discretized_graph.split_ratios.addNonCompliantSplitRatios(
        discretized_graph,
        data.non_compliant_split_ratios,
        discretized_graph.node_to_origin);
    System.out.println("Done");

    System.out.print("Initializing split-ratios at the origins...");
    splits =
        IntertemporalOriginsSplitRatios.defaultSplitRatios(
            time_discretization.getNb_steps(),
            discretized_graph.sources, alpha);
    System.out.println("Done");

    if (debug)
      System.out.println(splits.toString());

    System.out.print("Creating the compact representation...");
    lwr_network = new LWR_network(discretized_graph);
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

  public double getAlpha() {
    return alpha;
  }

  public boolean isFullSystemOptimal() {
    return alpha == 1.0;
  }
}
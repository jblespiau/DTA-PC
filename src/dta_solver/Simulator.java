package dta_solver;

import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.Demands;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.Graph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.Profile;
import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;

public class Simulator {

  DiscretizedGraph discretized_graph;
  public Discretization time_discretization;
  Demands origin_demands;
  IntertemporalOriginsSplitRatios splits;
  LWR_network lwr_network;
  public Profile[] profiles;

  protected Simulator(int delta_t, int nb_steps) {
    time_discretization = new Discretization(delta_t, nb_steps);
  }

  public Simulator(
      String network_file,
      String data_file,
      boolean debug) {

    double alpha = 1.0;

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

  public void run() {
    run(true);
  }

  public void run(boolean print) {
    profiles = new Profile[time_discretization.getNb_steps()];

    for (int k = 0; k < time_discretization.getNb_steps(); k++) {
      profiles[k] = lwr_network.emptyProfile();
    }

    for (int k = 0; k < time_discretization.getNb_steps(); k++) {
      if (k == 0) {
        profiles[k] = lwr_network.emptyProfile();
      } else if (k == 1) {
        profiles[k] = lwr_network.simulateProfileFrom(
            lwr_network.emptyProfile(),
            profiles[k - 1],
            time_discretization.getDelta_t(),
            origin_demands, splits,
            k - 1);
      } else {
        profiles[k] = lwr_network.simulateProfileFrom(profiles[k - 2],
            profiles[k - 1],
            time_discretization.getDelta_t(),
            origin_demands, splits,
            k - 1);
      }

      if (k > 0 && print) {
        System.out.println("****** Printing profile at time step " + (k - 1)
            + "********");
        profiles[k - 1].print();
      }
    }
  }
}

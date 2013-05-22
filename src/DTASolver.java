import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.Demands;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.Graph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.Profile;

import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;

import dta_solver.*;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    

    
    boolean debug = true;

    int nb_steps = 10;
    double delta_t = 1;
    int alpha = 1;
    Discretization time_discretization = new Discretization(delta_t, nb_steps);

    /* Creation of the network */
    JsonFactory json = new JsonFactory(true);

    System.out.print("Loading of the graph from JSON...");
    Graph json_graph = json.graphFromFile("graphs/parallelPath.json");
    System.out.println("Done");


    
    System.out.print("Discretization of the graph...");
    DiscretizedGraph discretized_graph = new DiscretizedGraph(json_graph,
        time_discretization.getDelta_t(), time_discretization.getNb_steps());
    System.out.println("Done");

    /* We load the data from the json file */
    System.out.print("Loading demands from JSON...");
    Json_data data = json.dataFromFile("graphs/parallelPathData.json");
    Demands origin_demands = new DemandsFactory(time_discretization,
        data.delta_t, data.demands, discretized_graph.node_to_origin)
        .buildDemands();
    System.out.println("Done");

    if (debug)
      System.out.println(origin_demands.toString());

    json.toFile(data, "graphs/test.json");

    System.out.println("Loading non-compliant internal split-ratios...");
    discretized_graph.split_ratios.addNonCompliantSplitRatios(
        discretized_graph,
        data.non_compliant_split_ratios,
        discretized_graph.node_to_origin);

    System.out.print("Initializing split-ratios at the origins...");
    IntertemporalOriginsSplitRatios splits =
        IntertemporalOriginsSplitRatios.defaultSplitRatios(
            time_discretization.getNb_steps(),
            discretized_graph.sources, alpha);
    System.out.println("Done");

    if (debug)
      System.out.println(splits.toString());

    System.out.print("Creating the compact representation...");
    LWR_network lwr_network = new LWR_network(discretized_graph);
    System.out.println("Done");

    if (debug) {
      System.out.println("Printing the compact form");
      lwr_network.print();
      lwr_network.printInternalSplitRatios();
    }

    Profile[] profiles = new Profile[time_discretization.getNb_steps()];

    for (int k = 0; k < time_discretization.getNb_steps(); k++) {
      profiles[k] = lwr_network.emptyProfile();
    }

    for (int k = 0; k < time_discretization.getNb_steps(); k++) {
      if (k == 0) {
        profiles[k] = lwr_network.emptyProfile();
      } else if (k == 1) {
        profiles[k] = lwr_network.simulateProfileFrom(
            lwr_network.emptyProfile(),
            profiles[k - 1], delta_t,
            origin_demands, splits,
            k - 1);
      } else {
        profiles[k] = lwr_network.simulateProfileFrom(profiles[k - 2],
            profiles[k - 1], delta_t,
            origin_demands, splits,
            k - 1);
      }

      if (k > 0) {
        System.out.println("****** Printing profile at time step " + (k - 1)
            + "********");
        profiles[k - 1].print();
      }
    }
    /*
     * DiscretizedGraph discretized_graph = new DiscretizedGraph(g, delta_t);
     * LWR_network lwr_network = new LWR_network(discretized_graph);
     * lwr_network.printNetwork();
     */
  }
}
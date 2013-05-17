import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.DemandFactory;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.Graph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.Profile;
import generalNetwork.state.splitRatios.IntertemporalOriginSplitRatios;
import model.networkFactory.NormalRoad;
import model.networkFactory.Path;

import dta_solver.*;

/**
 * @package module
 * @page intro Introduction
 * 
 *       The usual way to build a simulation is: - build a demand function (see
 *       Demand) - fix your time step size and number of steps of the problem
 *       (see Environment) - design your network - do just a simulation with an
 *       Origin in which you put your demand (get with buildDemand) or try to
 *       get the UE with adding a distributor and putting your buildDemand in it
 */
public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    int nb_steps = 10;
    double delta_t = 1;
    Discretization time_discretization = new Discretization(delta_t, nb_steps);
    // createDemand(time_discretization, 1);

    /* Creation of the network */
    JsonFactory json = new JsonFactory(true);

    System.out.print("Loading of the graph from JSON...");
    Graph json_graph = json.graphFromFile("graphs/parallelPath.json");
    System.out.println("Done");

    System.out.print("Discretization of the graph...");
    DiscretizedGraph discretied_graph = new DiscretizedGraph(json_graph,
        time_discretization.getDelta_t(), time_discretization.getNb_steps());
    System.out.println("Done");

    System.out.print("Creating the compact representation...");
    LWR_network lwr_network = new LWR_network(discretied_graph);
    System.out.println("Done");

    System.out.println("Printing the compact form");
    lwr_network.print();

    lwr_network.printInternalSplitRatios();

    Json_data demands = json.dataFromFile("graphs/parallelPathData.json");
    DemandsFactory df = new DemandsFactory(time_discretization,
        demands.delta_t, demands.demands, lwr_network.getJunctions());
    DemandFactory demand = new DemandFactory(time_discretization);

    System.out.println("Demands: " + df.toString());

    demand.add(0, 3);
    demand.add(1, 4);
    demand.add(2, 5);
    demand.display();
    double[] total_demand = demand.buildDemand();

    IntertemporalOriginSplitRatios splits =
        new IntertemporalOriginSplitRatios(time_discretization.getNb_steps());

    splits.automatic_uniform_distribution(0, 2,
        time_discretization.getNb_steps(), 1);

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
            total_demand[k - 1], splits.get(k - 1),
            k - 1);
      } else {
        profiles[k] = lwr_network.simulateProfileFrom(profiles[k - 2],
            profiles[k - 1], delta_t,
            total_demand[k - 1], splits.get(k - 1),
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

  public static void createDemand(Discretization time, int nb_origins) {
    DemandsFactory df = new DemandsFactory(time, nb_origins);

    DemandFactory demand = new DemandFactory(time);
    demand.add(0, 1);
    demand.add(1, 2);
    demand.add(3, 3);
    demand.add(4, 2);
    demand.add(5, 3);
    demand.add(6, 4);
    demand.add(7, 3);
    demand.add(8, 2);
    demand.add(9, 3);
    demand.add(10, 2);

    for (int i = 0; i < nb_origins; i++) {
      df.put(i, demand);
    }

    Json_data jsondata = new Json_data(df.buildDemands().buildJsonDemand(),
        null);
    JsonFactory json = new JsonFactory(true);
    json.toFile(jsondata, "test.json");
  }

  public static void parallelNetwork() {
    int nb_steps = 50;
    double delta_t = 1;

    /*
     * Creation of a simulator :
     * - create a new one with DTA_Simulator(delta_t, nb_of_steps)
     * - create the total flow demand
     * - create the network (to do so first create path and then add them in
     * the simulator)
     */
    DTA_ParallelSimulator simulator = new DTA_ParallelSimulator(delta_t,
        nb_steps);
    simulator.addDemandPoint(0, 10);
    simulator.addDemandPoint(1, 4);
    simulator.addDemandPoint(2, 3);
    simulator.addDemandPoint(3, 2);
    simulator.addDemandPoint(4, 3);
    simulator.addDemandPoint(5, 4);
    simulator.addDemandPoint(6, 5);
    simulator.addDemandPoint(7, 6);
    simulator.addDemandPoint(8, 7);
    simulator.addDemandPoint(9, 9);
    simulator.addDemandPoint(9, 9);
    simulator.addDemandPoint(10, 3);
    simulator.addDemandPoint(20, 3.3);
    simulator.addDemandPoint(30, 3);
    simulator.displayTotalDemand();

    /* Creation of the network */
    Graph g = new Graph(null);
    // DiscretizedGraph lwr = new DiscretizedGraph(g, delta_t);

    Path p1 = Path.SingleBottleneck(0.9 * 3, 0.9, 0.4, 2, 8, 1);
    Path p2 = Path.SingleBottleneck(1 * 3, 1, 0.4, 2, 8, 1);
    Path p3 = new Path(new NormalRoad(4, 1, 0.5, 3, 10));

    simulator.addPathToNetwork(p1);
    simulator.addPathToNetwork(p2);
    simulator.addPathToNetwork(p3);

    /*
     * The simulator has to be built after having fixed the discretization
     * and the network
     */
    simulator.buildSimulator();

    /***********************************************************
     * Checking the initial conditions and running the dynamic *
     ***********************************************************/
    /* Printing of the network to check it is the wanted one */

    simulator.printBuiltNetwork();
    simulator.checkContraints();

    /*********************************
     * Find User Equilibrium
     *********************************/
    /*
     * UE_Optimizer user_equilibrium = new UE_Optimizer(simulator);
     * 
     * 
     * double[] TT = user_equilibrium.computeLinksTT(0);
     * 
     * 
     * for (int i = 0; i < 10; i++) {
     * user_equilibrium.findOptimalSplitRatio(i);
     * }
     */
  }
}
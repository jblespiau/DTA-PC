import java.util.HashMap;
import java.util.LinkedList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import optimization.GradientDescent;
import optimization.GradientDescentMethod;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Destination;
import generalLWRNetwork.Junction;
import generalLWRNetwork.LWR_network;
import generalLWRNetwork.NetworkUIDFactory;
import generalLWRNetwork.Origin;
import generalLWRNetwork.RoadChunk;
import generalNetwork.data.Json_data;
import generalNetwork.data.demand.Demands;
import generalNetwork.data.demand.DemandsFactory;
import generalNetwork.graph.DisplayGUI;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.State;
import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
import graphics.GUI;

import dta_solver.Discretization;
import dta_solver.SOPC_Optimizer;
import dta_solver.SO_OptimizerByFiniteDifferences;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // EditorGUI e = new EditorGUI();
    // reportExample();
    // complexExample();

    ZiliaskopoulosNetwork();
  }

  public static void printExample() {
    String network_file = "graphs/drawing.json";
    String data_file = "graphs/drawingData.json";

    Simulator simulator = new Simulator(network_file, data_file, 0.9, true);

    State s = simulator.partialRun(true);

    DisplayGUI e = new DisplayGUI(simulator);
    e.displayState(s.get(15));
  }

  public static void optimizationExampleWithHomeMadeGradient() {
    /* Share of the compliant flow */
    double alpha = 1;
    boolean debug = false;
    // String network_file = "graphs/TwoParallelPath.json";
    // String data_file = "graphs/TwoParallelPathData.json";
    String network_file = "JUnitTests/2x1JunctionNetwork.json";
    String data_file = "JUnitTests/2x1JunctionNetworkData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 150;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void optimizationExampleByFiniteDifferences() {
    /* Share of the compliant flow */
    double alpha = 1;
    String network_file = "graphs/TwoParallelPath.json";
    String data_file = "graphs/TwoParallelPathData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, false);

    int maxIter = 50;
    SO_OptimizerByFiniteDifferences optimizer = new SO_OptimizerByFiniteDifferences(
        simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void complexExample() {
    /* Share of the compliant flow */
    double alpha = 1;
    String network_file = "graphs/ComplexNetwork.json";
    String data_file = "graphs/ComplexNetworkData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, true);

    int maxIter = 80;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescent homemade_test = new GradientDescent(maxIter);
    homemade_test.setGradient_condition(10E-9);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void reportExample() {
    System.out.println("*****************************************");
    System.out.println("   Optimization by the adjoint method    ");
    System.out.println("*****************************************");

    /* Share of the compliant flow */
    double alpha = 1;
    boolean debug = false;
    String network_file = "graphs/ReportExample.json";
    String data_file = "graphs/ReportExampleData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 70;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);

    JFreeChart display = ((GradientDescent) homemade_test).getChart();
    GUI g = new GUI();
    ChartPanel chartPanel = new ChartPanel(display);
    g.setContentPane(chartPanel);
    g.setVisible(true);
  }

  public static void ZiliaskopoulosNetwork() {

    /* Reset the unique id generators for cells and junctions */
    NetworkUIDFactory.resetCell_id();
    NetworkUIDFactory.resetJunction_id();

    int nb_time_steps = 18;
    double delta_t = 10;

    LinkedList<Cell> cell_list = new LinkedList<Cell>();
    LinkedList<Junction> junction_list = new LinkedList<Junction>();

    /* Creation of junctions */
    Junction j_0 = new Junction(0, 1);
    Junction j_1 = new Junction(1, 2);
    Junction j_2 = new Junction(1, 2);
    Junction j_3 = new Junction(2, 1);
    Junction j_4 = new Junction(1, 1);
    Junction j_5 = new Junction(2, 1);
    Junction j_6 = new Junction(1, 0);

    Junction[] junctions = { j_0, j_1, j_2, j_3, j_4, j_5, j_6 };
    for (int i = 0; i < junctions.length; i++)
      junction_list.add(junctions[i]);

    Origin o = new Origin(j_0, "SingleBuffer", cell_list, junction_list);
    /* Creation of cells */
    double l = 500, v = 50, w = 50, f_max = 0.5, jam_capacity = 0.02;
    RoadChunk cell_1 =
        new RoadChunk(l, v, w, 2 * f_max, 2 * jam_capacity, nb_time_steps);
    RoadChunk cell_2 =
        new RoadChunk(l, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_3 =
        new RoadChunk(l, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_4 =
        new RoadChunk(l, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_5 =
        new RoadChunk(l, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_6 =
        new RoadChunk(l, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_7 =
        new RoadChunk(500.00001, v, w, f_max, jam_capacity, nb_time_steps);
    RoadChunk cell_8 =
        new RoadChunk(l, v, w, 2 * f_max, 2 * jam_capacity, nb_time_steps);

    Cell[] cells =
    { cell_1, cell_2, cell_3, cell_4, cell_5, cell_6, cell_7, cell_8 };
    for (int i = 0; i < cells.length; i++)
      cell_list.add(cells[i]);

    /* Time dependent max flow */
    cell_5.F_max[2] = 0.000;
    cell_5.F_max[3] = 0.000;
    cell_5.F_max[4] = 0.3; // 0.3
    cell_5.F_max[5] = 0.3;// 0.3

    /* Linking cells to junctions */
    j_0.addNext(cell_1);

    j_1.addPrev(cell_1);
    cell_1.setNext(j_1);
    j_1.addNext(cell_2);
    j_1.addNext(cell_3);

    j_2.addPrev(cell_2);
    cell_2.setNext(j_2);
    j_2.addNext(cell_4);
    j_2.addNext(cell_5);

    j_3.addPrev(cell_3);
    cell_3.setNext(j_3);
    j_3.addPrev(cell_4);
    cell_4.setNext(j_3);
    j_3.addNext(cell_6);
    HashMap<Integer, Double> priorities = new HashMap<Integer, Double>(2);
    priorities.put(3, 0.5);
    priorities.put(4, 0.5);
    j_3.setPriorities(priorities);

    j_4.addPrev(cell_6);
    cell_6.setNext(j_4);
    j_4.addNext(cell_7);

    j_5.addPrev(cell_5);
    cell_5.setNext(j_5);
    j_5.addPrev(cell_7);
    cell_7.setNext(j_5);
    j_5.addNext(cell_8);
    priorities = new HashMap<Integer, Double>(2);
    priorities.put(5, 0.5);
    priorities.put(7, 0.5);
    j_5.setPriorities(priorities);

    j_6.addPrev(cell_8);
    cell_8.setNext(j_6);

    /* Destination */
    Destination d = new Destination(j_6, "SingleJunction", cell_list,
        junction_list);

    /* LWR_network graph */

    Origin[] origins = { o };
    Destination[] destinations = { d };

    /* We create the intertemporal split ratios */
    LinkedList<Integer> commodities_at_origin = new LinkedList<Integer>();
    o.setCompliant_commodities(commodities_at_origin);
    o.getCompliant_commodities();
    commodities_at_origin.add(1);
    commodities_at_origin.add(2);
    commodities_at_origin.add(3);
    IntertemporalSplitRatios split_ratios =
        new IntertemporalSplitRatios(junctions, nb_time_steps);
    /* Path 1 through 1->2->6 */
    split_ratios.addCompliantSRToJunction(1, 2, 1, 1, j_1);
    split_ratios.addCompliantSRToJunction(2, 5, 1, 1, j_2);
    /* Path 2 through 1->2->4 */
    split_ratios.addCompliantSRToJunction(1, 2, 2, 1, j_1);
    split_ratios.addCompliantSRToJunction(2, 4, 2, 1, j_2);
    /* Path 3 through 1->3 */
    split_ratios.addCompliantSRToJunction(1, 3, 3, 1, j_1);

    LWR_network graph = new LWR_network(cell_list, junction_list, origins,
        destinations, split_ratios, 3);
    graph.print();
    graph.printInternalSplitRatios();

    /* Creation of the simulator */
    Simulator simulator = Simulator.emptyObject(1);
    simulator.lwr_network = graph;
    simulator.time_discretization = new Discretization(delta_t, nb_time_steps);

    /* Creation of the demands */
    Demands origin_demands;
    JsonFactory json = new JsonFactory(true);
    Json_data data = json.dataFromFile("graphs/ZiliaskopoulosData.json");
    HashMap<Integer, Origin> node_to_origin = new HashMap<Integer, Origin>(1);
    node_to_origin.put(0, o);
    System.out.print("Loading demands from JSON...");
    origin_demands = new DemandsFactory(simulator.time_discretization,
        delta_t, data.demands, node_to_origin)
        .buildDemands();
    System.out.println("Done");

    System.out.println(origin_demands.toString());

    simulator.origin_demands = origin_demands;
    simulator.initializSplitRatios();

    System.out.println(simulator.splits.toString());

    /* Checking the requirements on the network */
    System.out
        .print("Checking that the network respect needed requirements...");
    graph.checkConstraints(delta_t);
    System.out.println("Done");

    /* Running the simulation */
    int maxIter = 1000;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescent homemade_test = new GradientDescent(maxIter);
    // homemade_test.setLineSearch(new LineSearch());
    homemade_test.setGradient_condition(10E-9);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);

    //State final_state = optimizer.forwardSimulate(result, true);
    //optimizer.printProperties(final_state);
    /*
     * 
     * 
     * double objective = 0;
     * 
     * for (int k = 0; k < nb_time_steps; k++) {
     * for (int cell_id = 0; cell_id < cells.length; cell_id++) {
     * if (cell_id != 9)
     * objective += final_state.profiles[k].getCell(cell_id).total_density;
     * }
     * }
     * 
     * System.out.println("Total objective: " + objective);
     */
  }
}
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
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
import generalNetwork.graph.MutableGraph;
import generalNetwork.graph.json.JsonFactory;
import generalNetwork.state.State;
import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
import graphics.GUI;

import dataStructures.HashMapPairCellsDouble;
import dataStructures.PairCells;
import dta_solver.Discretization;
import dta_solver.SOPC_Optimizer;
import dta_solver.SO_OptimizerByFiniteDifferences;
import dta_solver.Simulator;
import edu.berkeley.path.model_objects.MOException;

import edu.berkeley.path.model_objects.jaxb.DemandProfile;
import edu.berkeley.path.model_objects.jaxb.Link;
import edu.berkeley.path.model_objects.jaxb.FundamentalDiagramProfile;
import edu.berkeley.path.model_objects.jaxb.NetworkSet;
import edu.berkeley.path.model_objects.jaxb.Node;

import edu.berkeley.path.model_objects.scenario.Density;
import edu.berkeley.path.model_objects.scenario.Scenario;
import edu.berkeley.path.model_objects.scenario.ScenarioFactory;
import edu.berkeley.path.model_objects.scenario.Splitratio;
import edu.berkeley.path.model_objects.util.Serializer;

public class DTASolver {

  /**
   * @param args
   * @throws MOException
   * @throws IOException
   */
  public static void main(String[] args) throws IOException, MOException {
    // EditorGUI e = new EditorGUI();
    // reportExample();
    // complexExample();

    // ZiliaskopoulosNetwork();

    highwayNetwork();
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
    cell_5.F_max[2] = 0;
    cell_5.F_max[3] = 0;
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
    int maxIter = 100;
    SOPC_Optimizer optimizer = new SO_OptimizerByFiniteDifferences(simulator);

    GradientDescent homemade_test = new GradientDescent(maxIter);
    // homemade_test.setLineSearch(new LineSearch());
    homemade_test.setGradient_condition(10E-9);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);

    // State final_state = optimizer.forwardSimulate(result, true);
    // optimizer.printProperties(final_state);
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

  /**
   * @brief Running the optimization from a network from PATH
   * @details PATH code being closed, I have only added my interface to the git.
   * The following jar files are needed to be able to run it: commons-io.1.4.jar
   * model-objects-0.1-SNAPSHOT.jar, CORE-01-SNAPSHOT.jar, jettison-1.3.2.jar
   */
  public static void highwayNetwork() throws IOException, MOException {

    /* Reset the unique id generators for cells and junctions */
    NetworkUIDFactory.resetCell_id();
    NetworkUIDFactory.resetJunction_id();

    /*
     * We first need to import the network and data into a usable format.
     * To do so, we need to dig into the model object and grope for a working
     * method
     */
    System.out.print("Loading PATH xml file...");
    String xml = FileUtils.readFileToString(new File(
        "graphs/Rerouting_plus_BF_SR_InD_v3.xml"));
    Scenario scenario =
        Serializer.xmlToObject(xml, Scenario.class, new ScenarioFactory());
    System.out.println("Done.");

    System.out.print("Converting it into an usable network...");
    // We get the network
    NetworkSet network_set = scenario.getNetworkSet();
    List<edu.berkeley.path.model_objects.jaxb.Network> list = network_set
        .getNetwork();
    assert list.size() == 1;
    edu.berkeley.path.model_objects.jaxb.Network network = list.get(0);

    // We get the nodes
    Iterator<edu.berkeley.path.model_objects.jaxb.Node> node_iterator =
        network.getNodeList().getNode().iterator();
    HashMap<Integer, edu.berkeley.path.model_objects.jaxb.Node> PATH_nodes =
        new HashMap<Integer, edu.berkeley.path.model_objects.jaxb.Node>(
            network.getNodeList().getNode().size());
    edu.berkeley.path.model_objects.jaxb.Node tmp_node;
    while (node_iterator.hasNext()) {
      tmp_node = node_iterator.next();
      PATH_nodes.put((Integer) (int) tmp_node.getId(), tmp_node);
    }

    // We get the links
    Iterator<edu.berkeley.path.model_objects.jaxb.Link> link_iterator =
        network.getLinkList().getLink().iterator();
    HashMap<Integer, edu.berkeley.path.model_objects.jaxb.Link> PATH_links =
        new HashMap<Integer, edu.berkeley.path.model_objects.jaxb.Link>(
            network.getLinkList().getLink().size());
    edu.berkeley.path.model_objects.jaxb.Link tmp_link;
    while (link_iterator.hasNext()) {
      tmp_link = link_iterator.next();
      PATH_links.put((Integer) (int) tmp_link.getId(), tmp_link);
    }

    // We get the initial densities
    Iterator<Density> densities =
        scenario.getInitialDensitySet().getListOfDensities().iterator();

    HashMap<Integer, Double> initial_densities =
        new HashMap<Integer, Double>(10);
    Density tmp_density;
    while (densities.hasNext()) {
      tmp_density = densities.next();
      initial_densities.put((Integer) (int) tmp_density.getLinkId(),
          Double.parseDouble(tmp_density.getContent()));
    }
    System.out.println("Initial densities:" + initial_densities.toString());

    // We get the non-compliant split ratios
    assert scenario.getSplitRatioSet().getListOfSplitRatioProfiles().size() == 1 : "Zero or multiple split ratios profile not implemented yet";
    double split_ratios_dt = scenario
        .getSplitRatioSet()
        .getListOfSplitRatioProfiles()
        .get(0).getDt();
    System.out.println("Split ratios (dt =" + split_ratios_dt + "):");

    LinkedList<HashMapPairCellsDouble> SR_list =
        new LinkedList<HashMapPairCellsDouble>();

    Iterator<Splitratio> non_compliant_SR =
        scenario
            .getSplitRatioSet()
            .getListOfSplitRatioProfiles()
            .get(0)
            .getListOfSplitratios()
            .iterator();

    Splitratio tmp_SR;
    while (non_compliant_SR.hasNext()) {
      tmp_SR = non_compliant_SR.next();
      HashMapPairCellsDouble non_compliant_split_ratios;
      if (Integer.parseInt(tmp_SR.getIds()) == 1) {
        non_compliant_split_ratios = new HashMapPairCellsDouble();
        SR_list.add(non_compliant_split_ratios);
      } else {
        non_compliant_split_ratios = SR_list.getLast();
      }
      non_compliant_split_ratios.put(
          new PairCells((int) tmp_SR.getLinkIn(), (int) tmp_SR.getLinkOut()),
          Double.parseDouble(tmp_SR.getContent()));
    }

    HashMapPairCellsDouble[] SR_array =
        new HashMapPairCellsDouble[SR_list.size()];
    SR_list.toArray(SR_array);

    System.out
        .println("Non-compliant split ratios:" + Arrays.toString(SR_array));

    // Demand set
    Iterator<DemandProfile> it_demand =
        scenario.getDemandSet().getDemandProfile().iterator();
    HashMap<Integer, Double> demands = new HashMap<Integer, Double>();

    DemandProfile tmp_demand;
    while (it_demand.hasNext()) {
      tmp_demand = it_demand.next();
      assert tmp_demand.getDemand().size() == 1 : "Dealing with constant demand";
      demands.put((Integer) (int) tmp_demand.getLinkIdOrg(),
          Double.parseDouble(tmp_demand.getDemand().get(0).getContent()));
    }

    System.out.println("Demand profiles:" + demands.toString());

    // Fundamental Triangular Diagram
    Iterator<edu.berkeley.path.model_objects.scenario.FundamentalDiagramProfile> it_fdp =
        scenario
            .getFundamentalDiagramSet()
            .getListOfFundamentalDiagramProfiles()
            .iterator();

    HashMap<Integer, FundamentalDiagramProfile> fundamentalDiagrams =
        new HashMap<Integer, FundamentalDiagramProfile>(
            scenario
                .getFundamentalDiagramSet()
                .getListOfFundamentalDiagramProfiles().size());

    FundamentalDiagramProfile tmp_FDP;
    while (it_fdp.hasNext()) {
      tmp_FDP = it_fdp.next();
      fundamentalDiagrams.put((int) tmp_FDP.getId(), tmp_FDP);
      /*
       * double dt = tmp_FDP.getDt();
       * FundamentalDiagram fundamental_diagram = tmp_FDP
       * .getFundamentalDiagram()
       * .get(0);
       * double capacity = fundamental_diagram.getCapacity();
       * double congestion_speed = fundamental_diagram.getCongestionSpeed();
       * double free_flow_speed = fundamental_diagram.getFreeFlowSpeed();
       */
    }

    /*
     * We create the mutable graph:
     * - We add the good number of nodes and keep the relation between PATH
     * nodes and nodes
     * - We add the good number of links and keep the relation between PATH
     * links
     * - We ensure the update of the pointers to nodes and links
     */
    MutableGraph mutable_graph = new MutableGraph();

    /* We first add the nodes */
    Iterator<Node> all_nodes = PATH_nodes.values().iterator();
    HashMap<Integer, generalNetwork.graph.Node> PATHNode_to_nodes =
        new HashMap<Integer, generalNetwork.graph.Node>(PATH_nodes.size());

    while (all_nodes.hasNext()) {
      tmp_node = all_nodes.next();
      mutable_graph.addNode(0, 0);
      PATHNode_to_nodes.put((int) tmp_node.getId(),
          mutable_graph.getLastAddedNode());
    }

    /* We then add the links */
    Iterator<Link> all_links = PATH_links.values().iterator();
    HashMap<Integer, generalNetwork.graph.Link> PATHLink_to_links =
        new HashMap<Integer, generalNetwork.graph.Link>(PATH_links.size());

    while (all_links.hasNext()) {
      tmp_link = all_links.next();
      generalNetwork.graph.Node from, to;
      from = PATHNode_to_nodes.get((int) tmp_link.getBegin().getNodeId());
      assert from != null;
      to = PATHNode_to_nodes.get((int) tmp_link.getEnd().getNodeId());
      assert to != null;
      mutable_graph.addLink(from, to);

      PATHLink_to_links.put((int) tmp_link.getId(),
          mutable_graph.getLastAddedLink());

      generalNetwork.graph.Link tmp = mutable_graph.getLastAddedLink();
      from.addOutgoingLink(tmp);
      to.addIncomingLink(tmp);
    }

    assert mutable_graph.check() : "We should have nodes[i].id = i and links[i].id = i";
    // Then we create all the nodes
    // we create the hash map PATH_node -> JB_node
    // We create all the links with the right values of the incoming links,
    // We create the Hash map PATH_link -> JB_Link
    // We update the correct in and out link in the jb-nodes.
    // We create the graph.
    // We add the non-compliant split ratios
    // We also need to create the path !!!

    // We also need to discretize the demand and the non-compliant split ratios

    // Then the rest should be ok:
    // - we decide of a value for delta_t
    // - We discretize the network
    // - build the simulator from it, ...

    // the right fundamental diagram
    // We get the fundamental triangular diagram
    /*
     * Iterator<edu.berkeley.path.model_objects.jaxb.Link> iterator =
     * network.getLinkList().getLink().iterator();
     * 
     * edu.berkeley.path.model_objects.jaxb.Link tmp_link;
     * while (iterator.hasNext()) {
     * tmp_link = iterator.next();
     * 
     * }
     */
    System.out.println("Done.");
  }
}
import generalNetwork.graph.DisplayGUI;
import generalNetwork.graph.EditorGUI;
import generalNetwork.state.State;

import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //EditorGUI e = new EditorGUI();
    optimizationExample();
    //printExample();
  }

  public static void printExample() {
    String network_file = "graphs/drawing.json";
    String data_file = "graphs/drawingData.json";

    Simulator simulator = new Simulator(network_file, data_file, 0.9, true);

    State s = simulator.partialRun(true);

    DisplayGUI e = new DisplayGUI(simulator);
    e.displayState(s.get(15));
  }

  public static void optimizationExample() {
    /* Share of the compliant flow */
    double alpha = 1;
    boolean debug = false;
    String network_file = "graphs/TwoParallelPath.json";
    String data_file = "graphs/TwoParallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(maxIter, simulator);
    //SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(), maxIter, simulator);
    //optimizer.profileComputationTime();

    double[] final_control = optimizer.solve();

    State final_state = optimizer.forwardSimulate(final_control, false);

    optimizer.printProperties(final_state);
    optimizer.printFullControl();
  }
}
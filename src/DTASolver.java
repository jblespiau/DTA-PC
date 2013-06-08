import generalNetwork.state.State;

import org.wsj.IpOptAdjointOptimizer;

import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    /* Share of the non-compliant flow */
    double alpha = 0.9;
    boolean debug = false;
    String network_file = "graphs/parallelPath.json";
    String data_file = "graphs/parallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(),
        maxIter, simulator);

    double[] final_control = optimizer.solve();

    State final_state = optimizer.forwardSimulate(final_control, false);
    
    optimizer.printProperties(final_state);
    optimizer.printFullControl();
  }
}
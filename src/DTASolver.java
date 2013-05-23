import jUnit.TestSimulation;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    boolean debug = true;
    // String network_file = "graphs/parallelPath.json";
    // String data_file = "graphs/parallelPathData.json";
    String network_file = "graphs/PathWithPriorities.json";
    String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, debug);
    simulator.run();

    // TestSimulation.registerParallelPath();
  }
}
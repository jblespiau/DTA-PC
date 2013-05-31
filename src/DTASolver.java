import java.util.Arrays;

import org.wsj.IpOptAdjointOptimizer;

import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    boolean debug = true;
    String network_file = "graphs/parallelPath.json";
    String data_file = "graphs/parallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, debug);
    simulator.run();

    // TestSimulation.registerParallelPath();
    // Test2x1JunctionSolver.createProfile();
    // Test2x1JunctionSolver.register2x1Junction();

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(),
        maxIter, simulator);
    System.out.println(Arrays.toString(optimizer.getControl()));
    System.out.println(
        optimizer.djdx(simulator, optimizer.getControl()).toString());
    System.out.println(Arrays.toString(optimizer.djdu(simulator,
        optimizer.getControl()).toArray()));
    System.out.println(optimizer
        .dhdx(simulator, optimizer.getControl())
        .toString());
    double a = -1.0/0.0;
    System.out.println(Math.log(-1));
   // if (a == Double.POSITIVE_INFINITY || a == Double.NEGATIVE_INFINITY)
    // System.out.println("Infinity detected");
     optimizer.solve(optimizer.getControl());
  }
}
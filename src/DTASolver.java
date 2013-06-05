import io.InputOutput;
import generalNetwork.state.State;

import org.wsj.IpOptAdjointOptimizer;

import dataStructures.Numerical;
import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    /* Share of the non-compliant flow */
    double alpha = 0.9;
    boolean debug = true;
    String network_file = "graphs/parallelPath.json";
    String data_file = "graphs/parallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(),
        maxIter, simulator);

    double[] intial_control = optimizer.getControl();
    // InputOutput.printTable(intial_control);
    // System.exit(1);
    optimizer.printFullControl();
    // System.exit(1);
    State state = optimizer.forwardSimulate(intial_control);
    // System.out.println(Arrays.toString(optimizer.getControl()));
    // System.out.println(
    // optimizer.djdx(state, optimizer.getControl()).toString());

    // System.out.println(Arrays.toStrin g(optimizer.djdu(state,
    // optimizer.getControl()).toArray()));

    double[] tmp = optimizer.djdu(state, intial_control).toArray();
    for (int i = 0; i < tmp.length; i++)
      assert Numerical.validNumber(tmp[i]);

    // System.out.println(optimizer
    // .dhdx(state, optimizer.getControl())
    // .toString());

    /* Checking of dH/dX */
    double[][] tmp2 = optimizer.dhdx(state, intial_control).get().toArray();
    for (int i = 0; i < tmp2.length; i++)
      for (int j = 0; j < tmp2[0].length; j++)
        assert Numerical.validNumber(tmp2[i][j]);

    for (int i = 0; i < tmp2.length; i++) {
      if (tmp2[i][i] == 0)
        System.out.println(i);
    }

    double[] final_control = optimizer.solve(intial_control);

    State final_state = optimizer.forwardSimulate(final_control, true);
    // InputOutput.printControl(final_control);
    // InputOutput.tableToFile(final_control, final_control_file);
    optimizer.printProperties(final_state);

    optimizer.printFullControl();

    double a = 0.9;
  }
}
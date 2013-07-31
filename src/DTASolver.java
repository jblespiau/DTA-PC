import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import optimization.GradientDescent;
import optimization.GradientDescentMethod;
import generalNetwork.graph.DisplayGUI;
import generalNetwork.graph.EditorGUI;
import generalNetwork.state.State;
import graphics.GUI;

import dta_solver.SOPC_Optimizer;
import dta_solver.SO_OptimizerByFiniteDifferences;
import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // EditorGUI e = new EditorGUI();
    // reportExample();
    complexExample();
    /*
     * System.out.println("*****************************************");
     * System.out.println(" Gradient descent by finite differences  ");
     * System.out.println("*****************************************");
     * long startTime = System.currentTimeMillis();
     * optimizationExampleByFiniteDifferences();
     * long endTime = System.currentTimeMillis();
     * long searchTime = endTime - startTime;
     * System.out.println("Time (ms): " + searchTime);
     */
    /*
     * System.out.println("*****************************************");
     * System.out.println(" Gradient descent by the adjoint method  ");
     * System.out.println("*****************************************");
     * long startTime = System.currentTimeMillis();
     * optimizationExampleWithHomeMadeGradient();
     * long endTime = System.currentTimeMillis();
     * long searchTime = endTime - startTime;
     * System.out.println("Time (ms): " + searchTime);
     */
    // printExample();
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
    boolean debug = true;
    String network_file = "graphs/TwoParallelPath.json";
    String data_file = "graphs/TwoParallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(maxIter, simulator);
    // SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(),
    // maxIter, simulator);
    // optimizer.profileComputationTime();

    optimizer.printSizes();
    double[] final_control = optimizer.solve();

    State final_state = optimizer.forwardSimulate(final_control, true);

    optimizer.printProperties(final_state);
    optimizer.printFullControl();
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
    SOPC_Optimizer optimizer = new SOPC_Optimizer(maxIter, simulator);

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
        maxIter, simulator);

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
    SOPC_Optimizer optimizer = new SOPC_Optimizer(maxIter, simulator);

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
    SOPC_Optimizer optimizer = new SOPC_Optimizer(maxIter, simulator);

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
}
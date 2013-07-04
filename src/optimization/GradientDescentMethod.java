package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public class GradientDescentMethod {

  private int maxIterations = 100;
  /* The stopping criteria is || dJ/dx ||_2 < gradient_condition */
  private double gradient_condition = 10E-5;
  private LineSearchMethod lineSearch;
  private boolean verbose = true;

  public GradientDescentMethod() {
    super();
    lineSearch = new BackTrackingLineSearch();
  }

  public double[] solve(GradientDescentOptimizer function) {
    double[] control = function.getStartingPoint();
    double[] gradient = new double[control.length];

    System.out.println(
        "\n***************************\n" +
            " Gradient descent launched \n" +
            "***************************\n");
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: "
            + function.objective(control) + "\n");
      }

      /* Line search */
      /* Update x = x * t * delta_x; and J(x) */
      control = lineSearch.lineSearch(control, function);
      function.gradient(gradient, control);

      /* Stopping condition */
      if (stoppingTest(gradient)) {
        break;
      }
    }
    return control;
  }

  /**
   * @brief We stop if the 2 norm of the gradient is smaller than
   *        gradient_condition
   */
  private boolean stoppingTest(double[] gradient) {
    double tmp = 0;
    for (int i = 0; i < gradient.length; i++) {
      tmp += gradient[i] * gradient[i];
    }
    return tmp < gradient_condition;
  }

  public int getMaxIterations() {
    return maxIterations;
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  public LineSearchMethod getLineSearch() {
    return lineSearch;
  }

  public void setLineSearch(LineSearchMethod lineSearch) {
    this.lineSearch = lineSearch;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }
}
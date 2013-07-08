package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public class GradientDescent extends GradientDescentMethod {

  /* The stopping criteria is || dJ/dx ||_2 < gradient_condition */
  private double gradient_condition = 10E-5;

  public GradientDescent() {
    super();
    lineSearch = new BackTrackingLineSearch();
  }

  public GradientDescent(int maxIterations) {
    super(maxIterations);
    lineSearch = new BackTrackingLineSearch();
  }

  @Override
  public double[] solve(GradientDescentOptimizer function) {
    double[] control = function.getStartingPoint();
    double[] gradient = new double[control.length];

    System.out.println(
        "\n***************************\n" +
            " Gradient descent launched \n" +
            "***************************\n");
    for (int iteration = 1; iteration <= maxIterations; iteration++) {
      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: "
            + function.objective(control) + "\n");
      }

      /* Line search */
      /* Update x = x * t * delta_x; and J(x) */
      function.gradient(gradient, control);
      System.out.println("Gradient used in the descent");
      for (int i = 0; i < gradient.length; i++)
        System.out.println(gradient[i]);
      control = lineSearch.lineSearch(control, gradient, function);

      /* Stopping condition */
      if (stoppingTest(gradient)) {
        System.out
            .println("Stopping gradient descent because of nearly null gradient");
        for (int i = 0; i < gradient.length; i++)
          System.out.println(gradient[i]);
        System.out.println("If the gradient should not be null, it is" +
            "very likely that the number of time steps is not large" +
            "enough to allow all the vehicles to leave the network. \n" +
            "Try increasing the number of time steps");
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
    if (tmp < gradient_condition)
      System.out.println("Gradient condition: " + tmp);
    return tmp < gradient_condition;
  }
}
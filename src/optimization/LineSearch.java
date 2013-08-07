package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public class LineSearch implements LineSearchMethod {

  private double iteration_number = 10;

  public LineSearch() {
    super();
  }

  @Override
  public double[] lineSearch(double[] initial_point, double[] gradient,
      GradientDescentOptimizer function) {

    // System.out.println("Initial point");
    // for (int i = 0; i < initial_point.length; i++)
    // System.out.println("u(" + i + ")" + initial_point[i]);

    /* We compute the direction which is the opposite of the gradient */
    double[] direction = new double[initial_point.length];
   // System.out.println("Direction");
    for (int i = 0; i < gradient.length; i++) {
      direction[i] = -gradient[i];
      // System.out.println(direction[i]);
    }

    /* We initialize t such that the first point remain in the feasible set */
    double t = 1;
    /*
     * for (int i = 0; i < initial_point.length; i++)
     * // if (direction[i] > 0)
     * // t = Math.min(t, (1 - initial_point[i]) / direction[i]);
     * // else
     * if (direction[i] < 0) {
     * t = Math.min(t, (-initial_point[i] / direction[i]));
     * if (t < 10E-3) {
     * System.out.println("Very small t :" + t);
     * System.out.println((-initial_point[i]) + " divided by "
     * + direction[i]);
     * }
     * }
     * t *= 0.999999999;
     */

    if (t < 1.0 / iteration_number)
      System.out.println("1/t not taken. t=" + t);
    t = Math.min(t, 1.0 / Math.sqrt(iteration_number));
    iteration_number++;

    // System.out.println("Value of t: " + t);

    assert t > 0 : "Negative factor in backtracking line search";

    double[] temporary_position = new double[initial_point.length];
    for (int i = 0; i < temporary_position.length; i++) {
      temporary_position[i] = initial_point[i] + t * direction[i];
    }
    function.projectControl(temporary_position);

    return temporary_position;
  }
}
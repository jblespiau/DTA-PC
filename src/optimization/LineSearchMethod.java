package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public interface LineSearchMethod {

  /**
   * @brief Line search algorithm
   * @param initial_point
   *          The point from where the search begin
   * @param direction
   *          The direction of search
   * @param function The cost function
   * @return A point in the direction that has a smaller evaluation value
   */
  public double[] lineSearch(double[] initial_point, double[] init_gradient,
      GradientDescentOptimizer function);
}
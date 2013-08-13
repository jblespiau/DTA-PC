package dta_solver.adjointMethod;

import dataStructures.Tuple;


public interface CostFunction {

  /**
   * @brief Compute (J(x), dJ/dx(x)). To be used when we need both
   * @param x
   *          The point where we want to evaluate the cost function and the
   *          gradient
   * @return (Cost, Gradient)
   */
  public Tuple<Double, double[]> evaluateCost(double[] x);

  /**
   * @brief Compute the value J(x) for the function at the given point.
   */
  public double value(double[] point);

  /**
   * @brief Compute the gradient of the cost function dJ/dx(x) at the given point
   * @param x The point where the gradient has to be computed
   * @return The gradient vector
   */
  public double[] gradient(double[] x);
}
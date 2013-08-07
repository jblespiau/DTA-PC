package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

/**
 * @package optimization
 * @brief Contains the definitions of the gradient descent methods
 */

/**
 * @class GradientDescentMethod
 * @brief Abstract definition of a gradient descent
 */
public abstract class GradientDescentMethod {

  /** Maximum number of step */
  protected int maxIterations = 50;
  /** The line search method used in the gradient descent */
  protected LineSearchMethod lineSearch;
  protected boolean verbose = true;

  public GradientDescentMethod() {
  }

  public GradientDescentMethod(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  /**
   * @brief Solve the optimization problem using a gradient descent approach
   * @details The optimization will stop either when the maximum number of
   *          iterations is reached, or when other gradient descent specific
   *          stopping conditions are met
   * @param function
   *          The function to minimize
   * @return The control found by the optimizer that minimize the cost function
   */
  public abstract double[] solve(GradientDescentOptimizer function);

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
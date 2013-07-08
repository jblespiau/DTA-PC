package optimization;

import dta_solver.adjointMethod.GradientDescentOptimizer;

public abstract class GradientDescentMethod {

  protected int maxIterations = 50;
  protected LineSearchMethod lineSearch;
  protected boolean verbose = true;

  public GradientDescentMethod() {
  }

  public GradientDescentMethod(int maxIterations) {
    this.maxIterations = maxIterations;
  }

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
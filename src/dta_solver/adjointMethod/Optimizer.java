package dta_solver.adjointMethod;

public interface Optimizer {

  public void optimize(int maxIterations, CostFunction J, double[] startPoint);
}

package dta_solver;

import generalNetwork.state.State;

import java.util.Arrays;

import org.coinor.Ipopt;

import dta_solver.adjointMethod.IpOptOptimizer;

public class SO_OptimizerByFiniteDifferences extends SOPC_Optimizer {

  /** Maximum number of iterations */
  private int maxIter = 100;
  /** The Optimizer which has to be an IpOpt object for now */
  private IpOptOptimizer ipOpt;

  public SO_OptimizerByFiniteDifferences(Simulator simu) {
    super(simu);
    ipOpt = new IpOptOptimizer(this);
    simulator.initializSplitRatios();
  }

  /**
   * @details This function imposes that the control is physical (every split
   *          ratio is positive)
   */
  public double objective(double[] control) {
    /* Inforces control[i] >= 0, \forall i */
    for (int i = 0; i < control.length; i++)
      if (control[i] < 0)
        assert false : "Negative control " + control[i];
    State state = forwardSimulate(control);
    return simulator.objective(state);
  }

  public void notProjectedGradient(double[] gradient_f, double[] control) {
    double deviation = 0.001;
    double value = objective(control);
    for (int i = 0; i < control.length; i++) {
      double[] modified_control = Arrays.copyOf(control, control.length);
      modified_control[i] = modified_control[i] + deviation;

      double result = objective(modified_control);
      gradient_f[i] = (result - value) / deviation;
    }
  }

  /**
   * @brief Fill in the gradient with the values of the gradient
   * @param gradient_f
   *          The array that will contain the gradient
   * @param control
   *          The point where the gradient is computed
   */
  public void gradient(double[] gradient_f, double[] control) {
    double deviation = 0.001;
    double value = objective(control);
    double[] gradient = new double[gradient_f.length];
    for (int i = 0; i < control.length; i++) {
      double[] modified_control = Arrays.copyOf(control, control.length);
      modified_control[i] = modified_control[i] + deviation;

      double result = objective(modified_control);
      gradient[i] = (result - value) / deviation;
    }

    /* We project the gradient on the feasible space */
    projectGradient(gradient_f, gradient);
  }

  public void projectGradient(double[] gradient_f, double[] init_gradient) {
    for (int k = 0; k < T; k++) {
      int index = 0;
      for (int o = 0; o < O; o++) {
        double average = 0;
        int nb_commodities = sources[o].getCompliant_commodities().size();
        if (nb_commodities == 0) {
          System.out
              .println("[Warning] In Computation of the gradient by finite diff. 0 commodities");
          continue;
        }

        for (int c = 0; c < nb_commodities; c++) {
          average += init_gradient[k * C + index + c];
        }
        average /= nb_commodities;

        for (int c = 0; c < nb_commodities; c++)
          gradient_f[k * C + index + c] = init_gradient[k * C + index + c]
              - average;
        index += nb_commodities;
      }
    }
  }

  public double[] optimize(double[] startPoint) {

    int n = getStartingPoint().length;
    // solver.fn = f
    // solver.u0 = startPoint
    ipOpt.create(n, 0, 0, 0, Ipopt.C_STYLE);

    ipOpt.setStringOption(Ipopt.KEY_MU_STRATEGY, "adaptive");
    ipOpt.setStringOption(Ipopt.KEY_HESSIAN_APPROXIMATION,
        "limited-memory");
    ipOpt.setIntegerOption(Ipopt.KEY_MAX_ITER, maxIter);

    int status = ipOpt.OptimizeNLP();
    System.out.println("status");
    System.out.println(status);

    if (status <= 0 || status >= 0)
      return ipOpt.getState();
    else {
      System.out.println("Status != 0");
      return null;
    }
  }

  public int getMaxIter() {
    return maxIter;
  }

  public void setMaxIter(int maxIter) {
    this.maxIter = maxIter;
  }
}
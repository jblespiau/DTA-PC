package dta_solver;

import generalNetwork.state.State;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;

public class SOPC_Optimizer extends SO_Optimizer {

  public SOPC_Optimizer(int maxIter, Simulator simu) {
    super(maxIter, simu);

    simulator.initializSplitRatios();
  }

  /**
   * @brief Computes the derivative dJ/dU
   * @details
   *          The condition \beta >= 0 is already put in the solver (in
   *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
   *          in J
   */
  @Override
  public DoubleMatrix1D djdu(State state, double[] control) {
    return DoubleFactory1D.dense.make(T * temporal_control_block_size);
  }

  /**
   * @details This function imposes that the control is physical (every split
   *          ratio is positive)
   */
  @Override
  public double objective(double[] control) {
    /* Inforces control[i] >= 0, \forall i */
    for (int i = 0; i < control.length; i++)
      if (control[i] < 0)
        assert false : "Negative control " + control[i];
    // return Double.MAX_VALUE;

    return objective(forwardSimulate(control), control);
  }

  /**
   * @brief Computes the objective function:
   *        \sum_(i,c,k) \rho(i,c,k)
   *        - \sum_{origin o} epsilon2 * ln(\sum \rho(o,c,k) - 1)
   * @details
   *          The condition \beta >= 0 is already put in the solver (in
   *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
   *          in J
   */
  public double objective(State state, double[] control) {
    double objective = 0;

    /*
     * To compute the sum of the densities ON the network, we add the density of
     * all the cells and then remove the density of the sinks
     */
    for (int k = 0; k < T; k++) {
      for (int cell_id = 0; cell_id < cells.length; cell_id++)
        objective += state.profiles[k].getCell(cell_id).total_density;

      for (int d = 0; d < destinations.length; d++)
        objective -= state.profiles[k].getCell(destinations[d].getUniqueId()).total_density;
    }

    return objective;
  }

  /**
   * @brief We project the gradient given by the adjoint
   */
  public void gradient(double[] gradient_f, double[] control) {
    System.out.println("Control");
    for (int i = 0; i < control.length; i++)
      System.out.println(control[i]);
    super.gradient(gradient_f, control);
    System.out.println("Gradient");
    for (int i = 0; i < gradient_f.length; i++)
      System.out.println(gradient_f[i]);
    projectGradient(gradient_f, gradient_f);
  }

  private void projectGradient(double[] gradient_f, double[] init_gradient) {
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
}
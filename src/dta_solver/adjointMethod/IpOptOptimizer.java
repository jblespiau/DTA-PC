package dta_solver.adjointMethod;

import org.coinor.Ipopt;

/**
 * @brief This is a general optimization of a multi variables function objective
 *        using a gradient descent algorithm.
 *        The function is restricted to (R+)^n
 * @details The documentation of the Ipopt java interface can be found
 *          <a href="https://projects.coin-or.org/Ipopt/wiki/JavaInterface">
 *          here </a>
 *          It is not very clear, but following the example HS071 works.
 */
public class IpOptOptimizer extends Ipopt {

  /**
   * The black box giving the initial point, the value and the gradient of the
   * cost function
   */
  private GradientDescentOptimizer gradient_descent;

  public IpOptOptimizer(GradientDescentOptimizer gradient_descent) {
    super();
    this.gradient_descent = gradient_descent;
  }

  /** To add lower and upper bounds for variables */
  @Override
  protected boolean get_bounds_info(int n, double[] x_l, double[] x_u, int m,
      double[] g_l, double[] g_u) {
    // We set only the constraint 0 <= x_i <= 1000
    for (int i = 0; i < x_l.length; i++) {
      x_l[i] = 0;
      x_u[i] = 1000;
    }
    return true;
  }

  /** This function is used by IpOpt to get the initial point */
  @Override
  protected boolean get_starting_point(int n, boolean init_x, double[] x,
      boolean init_z, double[] z_L, double[] z_U, int m, boolean init_lambda,
      double[] lambda) {
    double[] starting_point = gradient_descent.getStartingPoint();
    assert (x.length == starting_point.length);
    for (int i = 0; i < x.length; i++)
      x[i] = starting_point[i];

    return true;
  }

  /**
   * The value of the evaluation of the function at the point x must be put in
   * obj_value[0].
   */
  @Override
  protected boolean eval_f(int n, double[] x, boolean new_x, double[] obj_value) {
    obj_value[0] = gradient_descent.objective(x);
    return true;
  }

  /** The value of the gradient at x must be put in grad_f */
  @Override
  protected boolean eval_grad_f(int n, double[] x, boolean new_x,
      double[] grad_f) {
    gradient_descent.gradient(grad_f, x);
    return true;
  }

  /** Used to define some constraints. Not used here ? */
  @Override
  protected boolean eval_g(int n, double[] x, boolean new_x, int m, double[] g) {
    return true;
  }

  /** Used to define the jacobian of the constraints. Not used here */
  @Override
  protected boolean eval_jac_g(int n, double[] x, boolean new_x, int m,
      int nele_jac, int[] iRow, int[] jCol, double[] values) {
    return true;
  }

  /** Hessian evaluation. Not used here */
  @Override
  protected boolean eval_h(int n, double[] x, boolean new_x, double obj_factor,
      int m, double[] lambda, boolean new_lambda, int nele_hess, int[] iRow,
      int[] jCol, double[] values) {
    return true;
  }
}

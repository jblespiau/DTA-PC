package dta_solver.adjointMethod;

import org.coinor.Ipopt;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.SparseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;

public abstract class Adjoint<T extends JavaSystemState> implements
    GradientDescentOptimizer {

  /** Linear algebraic matrix operations operating on sparse matrices */
  SparseDoubleAlgebra algebra = new SparseDoubleAlgebra();
  /** Linear algebraic matrix operations operating on dense matrices */
  DenseDoubleAlgebra dAlg = new DenseDoubleAlgebra();
  /** Maximum number of iterations */
  private int maxIter = 100;
  /** The Optimizer which has to be an IpOpt object for now */
  private IpOptOptimizer ipOptOptimizer;

  public Adjoint(int maxIter) {
    algebra = new SparseDoubleAlgebra();
    dAlg = new DenseDoubleAlgebra();
    this.maxIter = maxIter;
    ipOptOptimizer = new IpOptOptimizer(this);
  }

  public abstract SparseCCDoubleMatrix2D dhdx(T state, double[] control);

  public abstract SparseCCDoubleMatrix2D dhdu(T state, double[] control);

  public abstract SparseDoubleMatrix1D djdx(T state, double[] control);

  public abstract DoubleMatrix1D djdu(T state, double[] control);

  public abstract T forwardSimulate(double[] control);

  /**
   * @brief Fill in the gradient with the values of the gradient
   * @param gradient_f
   *          The array that will contain the gradient
   * @param control
   *          The point where the gradient is computed
   */
  public void gradient(double[] gradient_f, double[] control) {
    T state = forwardSimulate(control);
    DoubleMatrix1D djduSln = djdu(state, control);
    DoubleMatrix1D lambda = adjointVector(state, control);
    SparseCCDoubleMatrix2D dhduT = dhdu(state, control).getTranspose();
    // C = A x B; Equivalent to A.zMult(B,C,1,0,false,false).
    DoubleMatrix1D right = dAlg.mult(dhduT, lambda);
    DoubleMatrix1D grad = djduSln.assign(right, DoubleFunctions.minus);
    double[] temp = grad.toArray();
    for (int i = 0; i < temp.length; i++)
      gradient_f[i] = temp[i];
  }

  public DoubleMatrix1D adjointVector(T state, double[] control) {
    SparseCCDoubleMatrix2D dhdxTranspose = dhdx(state, control).getTranspose();
    SparseDoubleMatrix1D djdx = djdx(state, control);
    return algebra.solve(dhdxTranspose, djdx);

    // return Dcs_lsolve.cs_lsolve(dhdxTranspose, djdx);
  }

  public double[] optimize(double[] startPoint) {

    int n = getStartingPoint().length;
    System.out.println("Size of the starting point " + n);
    // solver.fn = f
    // solver.u0 = startPoint
    ipOptOptimizer.create(n, 0, 0, 0, Ipopt.C_STYLE);
    
    ipOptOptimizer.setStringOption(Ipopt.KEY_MU_STRATEGY, "adaptive");
    ipOptOptimizer.setStringOption(Ipopt.KEY_HESSIAN_APPROXIMATION,
        "limited-memory");
    ipOptOptimizer.setIntegerOption(Ipopt.KEY_MAX_ITER, maxIter);

    System.out.println("Pop");
    int status = ipOptOptimizer.OptimizeNLP();
    System.out.println("Pop2");
    System.out.println("status");
    System.out.println(status);

    if (status <= 0 || status >= 0)
      return ipOptOptimizer.getState();
    else {
      System.out.println("Status != 0");
      return null;
    }

  }

  /*
   * public double[] solve(double[] control0) {
   * T currentState = forwardSimulate(control0);
   * public void updateState(double[] control) {
   * currentState = forwardSimulate(control);
   * }
   * 
   * val outer = this
   * 
   * val diffFunction = new DifferentiableMultivariateFunction {
   * def gradient(): MultivariateVectorFunction = new MultivariateVectorFunction
   * {
   * def value(double[] point) = {
   * outer.gradient(point).toArray
   * }
   * }
   * def value(double[] point) = {
   * updateState(point);
   * val obj = objective(currentState, point)
   * obj
   * }
   * 
   * def partialDerivative(k: Int) = null
   * 
   * }
   * 
   * // return final sln given from optimizer of your choosing
   * optimizer.optimize(maxIter, diffFunction, GoalType.MINIMIZE,
   * control0).getPoint
   * }
   */

  public int getMaxIter() {
    return maxIter;
  }

  public void setMaxIter(int maxIter) {
    this.maxIter = maxIter;
  }
}

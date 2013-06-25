package jUnit;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import dta_solver.adjointMethod.Adjoint;

/**
 * @brief Test the adjoint method to compute the derivative term.
 *        We check with linear constant constraints.
 */
public class TestAdjoint extends Adjoint<JUnitState> {

  public TestAdjoint() {
    super(3);
  }

  private static Random random;
  private static DoubleMatrix2D Ju, Jx, Hx, Hu;
  private static DoubleMatrix1D U;
  private static DenseDoubleAlgebra algebra;
  private static int u = 10;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    random = new Random(424242);
    /** Linear algebraic matrix operations operating on sparse matrices */
    algebra = new DenseDoubleAlgebra();

    Ju = randomMatrixVector(u);
    Jx = randomMatrixVector(u);
    Hx = randomInvertibleMatrix(u, u);
    Hu = randomInvertibleMatrix(u, u);
    U = randomVector(u);
  }

  /**
   * @return A lower triangular invertible matrix of size IxJ
   */
  private static DoubleMatrix2D randomInvertibleMatrix(int I, int J) {
    DoubleMatrix2D result = new DenseDoubleMatrix2D(I, J);

    for (int i = 0; i < I; i++)
      for (int j = 0; j <= i; j++) {
        if (i == j)
          result.setQuick(i, j, random.nextDouble());
        else
          result.setQuick(i, j, 1.0);

      }
    return result;
  }

  /**
   * @return A vector of size I
   */
  private static DoubleMatrix2D randomMatrixVector(int I) {
    DoubleMatrix2D result = new DenseDoubleMatrix2D(1, I);

    for (int i = 0; i < I; i++)
      result.setQuick(0, i, random.nextDouble());
    return result;
  }

  private static DoubleMatrix1D randomVector(int I) {
    DoubleMatrix1D result = new DenseDoubleMatrix1D(I);

    for (int i = 0; i < I; i++)
      result.setQuick(i, random.nextDouble());
    return result;
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    random = null;
  }

  @Test
  public void testAdjoint() throws Exception {

    for (int t = 50; t < 120; t++) {
      TestAdjoint.u = t;
      TestAdjoint.setUpBeforeClass();
      System.out.println("Checking with random matrix of size " + t);
      double[] correct_gradient = new double[(int) U.size()];
      DoubleMatrix2D HxInvert = algebra.inverse(Hx);
      DoubleMatrix2D right = Jx.zMult(HxInvert, null).zMult(Hu, null);

      for (int i = 0; i < correct_gradient.length; i++)
        correct_gradient[i] = Ju.get(0, i) - right.get(0, i);
      // InputOutput.printTable(correct_gradient);

      double[] gradient = new double[(int) U.size()];
      gradient(gradient, null);
      // InputOutput.printTable(gradient);

      assertTrue(DebugFunctions.close(gradient, correct_gradient, 10E-3));
    }
  }

  @Override
  public double objective(double[] x) {
    return 0;
  }

  @Override
  public double[] getStartingPoint() {
    return new double[(int) U.size()];
  }

  @Override
  public SparseCCDoubleMatrix2D dhdx(JUnitState state, double[] control) {
    return new SparseCCDoubleMatrix2D(Hx.toArray());
  }

  @Override
  public SparseCCDoubleMatrix2D dhdu(JUnitState state, double[] control) {
    return new SparseCCDoubleMatrix2D(Hu.toArray());
  }

  @Override
  public SparseDoubleMatrix1D djdx(JUnitState state, double[] control) {
    return new SparseDoubleMatrix1D(Jx.toArray()[0]);
  }

  @Override
  public DoubleMatrix1D djdu(JUnitState state, double[] control) {
    return new SparseDoubleMatrix1D(Ju.toArray()[0]);
  }

  @Override
  public JUnitState forwardSimulate(double[] control) {
    JUnitState state = new JUnitState();
    state.state = new double[(int) U.size()];
    return state;
  }
}
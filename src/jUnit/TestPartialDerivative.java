package jUnit;

import static org.junit.Assert.*;
import io.InputOutput;
import generalLWRNetwork.Junction;
import generalNetwork.state.CellInfo;
import generalNetwork.state.State;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import dataStructures.Numerical;
import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

/**
 * @brief A simple test the success of which is no sign of correctness. But in
 *        case of failure, something is broken
 * @details This test the partial derivative terms for only 1 very simple
 *          network.
 *          In particular, it does only test 1x1 junction and 1x2 junctions and
 *          is never jammed. This was just conceived to do some minor testing
 *          but
 *          now it is done it is still a correct test
 */
public class TestPartialDerivative {

  private static Simulator simulator;
  private static SO_Optimizer optimizer;
  private static double[] control;
  private static State state;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    /* We are running a full SO (Share of the compliant flow= 1) */
    double alpha = 1;
    boolean debug = false;
    String network_file = "JUnitTests/TwoParallelPath.json";
    String data_file = "JUnitTests/TwoParallelPathData.json";

    simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 20;

    optimizer = new SO_Optimizer(maxIter, simulator);
    optimizer.printSizes();

    control = optimizer.getControl();
    control[0] = 0.8;
    control[1] = 0.30;
    state = optimizer.forwardSimulate(control, false);

    System.out.println("Control");
    optimizer.printFullControl();

    System.out.println("Gradient:");
    double[] gradient = new double[control.length];
    optimizer.gradient(gradient, control);
    InputOutput.printTable(gradient);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    simulator = null;
    optimizer = null;
    control = null;
    state = null;
  }

  @Test
  public void testdhdu() {
    double[][] dhdu = optimizer.dhdu(state, control).toArray();
    double[][] correct = correctdHdu(simulator, state);
    assertTrue(DebugFunctions.compareTable(dhdu, correct));
    System.out.println("Checking of dH/du: OK");
  }

  private double[][] correctdHdu(Simulator simu, State state) {
    int T = 3;
    int H_constraint_size = 60;
    int density_position = 0;
    int demand_supply_position = 15;
    int aggregate_SR_position = 15 + 10;
    int f_out_position = 15 + 10 + 5;
    int f_in_position = 15 + 10 + 5 + 15;
    assert (f_out_position == 30);
    assert (f_in_position == 45);

    double[][] result = new double[T * H_constraint_size][control.length];
    result[10][0] = 0.1;
    result[11][1] = 0.1;
    return result;
  }

  @Test
  public void testdJdu() {
    double[] djdu = optimizer.djdu(state, control).toArray();
    double[] correct = correctdJdu(simulator, state);
    assertTrue(DebugFunctions.compareTable(djdu, correct));
    System.out.println("Checking of dJ/du: OK");
  }

  private double[] correctdJdu(Simulator simu, State state) {
    int T = 3;
    double[] result = new double[control.length];
    result[0] = -0.01;
    result[1] = -0.01;
    return result;
  }

  @Test
  public void testdJdx() {
    double[] djdx = optimizer.djdx(state, control).toArray();
    double[] correct = correctdJdx(simulator, state);
    assertTrue(DebugFunctions.compareTable(djdx, correct));
    System.out.println("Checking of dJ/dx: OK");
  }

  private double[] correctdJdx(Simulator simu, State state) {
    int T = 3;
    int H_constraint_size = 60;

    double[] result = new double[T * H_constraint_size];
    for (int k = 0; k < T; k++)
      for (int cell = 0; cell < 5; cell++)
        for (int c = 0; c < 3; c++)
          result[k * H_constraint_size + cell * 3 + c] = 1.0;

    return result;
  }

  private void printTable(double[][] t) {
    for (int i = 0; i < t.length; i++)
      for (int j = 0; j < t[0].length; j++)
        if (t[i][j] != 0)
          System.out.println("(" + i + ", " + j + ") : " + t[i][j]);
  }

  private boolean compareTable(SparseCCDoubleMatrix2D t1,
      SparseCCDoubleMatrix2D t2) {
    return DebugFunctions.compareTable(t1.toArray(), t2.toArray());
  }
}
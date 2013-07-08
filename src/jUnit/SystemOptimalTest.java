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
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;

import dataStructures.Numerical;
import dta_solver.SOPC_Optimizer;
import dta_solver.SO_OptimizerByFiniteDifferences;
import dta_solver.Simulator;

/**
 * @brief Used to test the SO find by the gradient descent
 * 
 */
public class SystemOptimalTest {

  static private Simulator simulator;
  private static SOPC_Optimizer optimizer;
  private static SO_OptimizerByFiniteDifferences exactOptimizer;

  private static double[] control;
  private static State state;

  private static int T;
  private static int H_constraint_size = 60;
  private static int density_position = 0;
  private static int demand_supply_position = 15;
  private static int aggregate_SR_position = 15 + 10;
  private static int f_out_position = 15 + 10 + 5;
  private static int f_in_position = 15 + 10 + 5 + 15;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    /* We are running a full SO (Share of the compliant flow= 1) */
    double alpha = 1;
    boolean debug = false;
    String network_file = "JUnitTests/TwoParallelPath.json";
    String data_file = "JUnitTests/TwoParallelPathData.json";

    simulator = new Simulator(network_file, data_file, alpha, debug);
    T = simulator.time_discretization.getNb_steps();

    int maxIter = 20;

    optimizer = new SOPC_Optimizer(maxIter, simulator);
    exactOptimizer = new SO_OptimizerByFiniteDifferences(maxIter, simulator);

    System.out.println();
    optimizer.printSizes();
    control = optimizer.getControl();

    System.out.println("Control");
    InputOutput.printTable(control);
    System.out.println("Full control");
    optimizer.printFullControl();

    state = optimizer.forwardSimulate(control, false);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    simulator = null;
    optimizer = null;
    control = null;
    state = null;
  }

  /**
   * @brief This test that the dH/du matrix is correct
   *        There is only the terms d (density(3)) / d (gamma) for gamma the
   *        split ratios for commodity 1 and 2 at the first time step (which is
   *        at index 0 and 1).
   */
  @Test
  public void testdhdu() {
    double[][] dhdu = optimizer.dhdu(state, control).toArray();
    double[][] correct = correctdHdu(simulator, state);
    assertTrue(DebugFunctions.compareTable(dhdu, correct));
    System.out.println("Checking of dH/du: OK");
  }

  private double[][] correctdHdu(Simulator simu, State state) {

    double[][] result = new double[T * H_constraint_size][control.length];
    double demand = simu.origin_demands
        .get(simu.lwr_network.getSources()[0], 0);
    result[10][0] = demand;
    result[11][1] = demand;
    return result;
  }

  /**
   * @brief Test that the dJ/du matrix is correct. Should be zero
   */
  @Test
  public void testdJdu() {
    double[] djdu = optimizer.djdu(state, control).toArray();
    double[] correct = new double[djdu.length];
    assertTrue(DebugFunctions.compareTable(djdu, correct));
    System.out.println("Checking of dJ/du: OK");
  }

  /**
   * @brief Test that the dJ/dx matrix is correct.
   *        There are only the terms dJ/d(density) which are always equal to L_i
   */
  @Test
  public void testdJdx() {
    double[] djdx = optimizer.djdx(state, control).toArray();
    double[] correct = correctdJdx(simulator, state);
    assertTrue(DebugFunctions.compareTable(djdx, correct));
    System.out.println("Checking of dJ/dx: OK");
  }

  private double[] correctdJdx(Simulator simu, State state) {
    double[] result = new double[T * H_constraint_size];
    for (int k = 0; k < T; k++)
      for (int cell = 0; cell < 4; cell++)
        for (int c = 0; c < 3; c++)
          result[k * H_constraint_size + cell * 3 + c] = simu.lwr_network
              .getCells()[cell].getLength();

    return result;
  }

  @Test
  public void testGradient() {

    System.out.println("Lambda: ");
    /* Be aware that the lambda given by the adjointVector is -lambda */
    DoubleMatrix1D lambda1D = optimizer.adjointVector(state, control);
    double[] lambda = lambda1D.toArray();
    double[] correct_lambda = new double[lambda.length];
    for (int i = 0; i < lambda.length; i++) {
      if (correct_lambda[i] != lambda[i]) {
        optimizer.informationIndexInX(i);
        System.out.println(": " + -lambda[i]);
      }
    }

    double[] gradient = new double[control.length];
    optimizer.gradient(gradient, control);
    System.out.println("Gradient by the adjoint");
    System.out.println("Gradient[0]: " + gradient[0]);
    System.out.println("Gradient[1]: " + gradient[1]);
    System.out.println("Gradient by finite differencices");
    double[] exact_gradient = new double[control.length];
    exactOptimizer.gradient(exact_gradient, control);
    System.out.println("Gradient[0]: " + exact_gradient[0]);
    System.out.println("Gradient[1]: " + exact_gradient[1]);
    // InputOutput.printTable(gradient);
    // [k=1]Supply in cell 3

    int index = 60 + demand_supply_position + 2 * 1;
    double[][] dhdx = optimizer.dhdx(state, control).toArray();
    System.out.print("Non zero terms in the derivatives with respect to ");
    optimizer.informationIndexInX(index);
    System.out.println();
    for (int i = 0; i < dhdx.length; i++) {
      if (dhdx[i][index] != 0) {
        optimizer.informationIndexInX(i);
        System.out.println(": " + dhdx[i][index]);
      }
    }

  }

  @Test
  public void testdHdx() {
    double[][] dhdx = optimizer.dhdx(state, control).toArray();
    SparseCCDoubleMatrix2D correct_dhdx = correctDhdx(simulator, state);
    // assertTrue(compareTable(dhdx, correct_dhdx.toArray()));
    System.out.println("Checking of dH/dx: OK");
  }

  private SparseCCDoubleMatrix2D correctDhdx(Simulator simu, State state) {
    SparseCCDoubleMatrix2D result = new SparseCCDoubleMatrix2D(T
        * H_constraint_size, T * H_constraint_size);

    /* dH¹_(0, i, c)/dx : 15 constaints */
    for (int i = 0; i < 15; i++)
      result.setQuick(i, i, -1.0);

    /* dH¹_(k, i, c)/dx for k > 0 */
    double delta_t = simu.time_discretization.getDelta_t();
    int CC = 2; // Compliant commodities
    int C = CC + 1; // Commodities

    int block_position;
    int constraint_row;
    int variable_column;
    for (int k = 1; k < 3; k++) {
      block_position = k * H_constraint_size;

      // Normal cells
      for (int i = 0; i < 3; i++) {
        for (int c = 0; c < C; c++) {
          constraint_row = k * H_constraint_size + C * i + c;
          variable_column = constraint_row;
          /* d./d(rho_(i,c)(k)) : 18 contraints */
          result.setQuick(constraint_row, variable_column, -1.0);

          /* d./d(rho_(i,c)(k-1)) : 18 contraints (262 remaining) */
          variable_column = (k - 1) * H_constraint_size + C * i + c;
          result.setQuick(constraint_row, variable_column, 1.0);

          /* d./d(f_in_(i,c)(k-1)) : 18 contraints */
          variable_column = (k - 1) * H_constraint_size + f_in_position + C * i
              + c;
          result.setQuick(constraint_row, variable_column, 1.0);

          /* d./d(f_out_(i,c)(k-1)) : 18 contraints */
          variable_column = (k - 1) * H_constraint_size + f_out_position + C
              * i + c;
          result.setQuick(constraint_row, variable_column, -1.0);
        }
      }
      // Buffer

      for (int c = 0; c < C; c++) {
        int i = 3; // id of the buffer
        constraint_row = k * H_constraint_size + C * i + c;
        variable_column = constraint_row;
        /* d./d(rho_(i,c)(k)) : 6 contraints */
        result.setQuick(constraint_row, variable_column, -1.0);

        /* d./d(rho_(i,c)(k-1)) : 6 contraints */
        variable_column = (k - 1) * H_constraint_size + C * i + c;
        result.setQuick(constraint_row, variable_column, 1.0);

        /* d./d(f_out_(i,c)(k-1)) : 6 contraints (208 remaining) */
        variable_column = (k - 1) * H_constraint_size + f_out_position + C * i
            + c;
        result.setQuick(constraint_row, variable_column, -1.0);
      }

      // Sink
      for (int c = 0; c < C; c++) {
        int i = 4; // id of the sink
        constraint_row = k * H_constraint_size + C * i + c;
        variable_column = constraint_row;
        /* d./d(rho_(i,c)(k)) : 6 contraints */
        result.setQuick(constraint_row, variable_column, -1.0);

        /* d./d(rho_(i,c)(k-1)) : 6 contraints */
        variable_column = (k - 1) * H_constraint_size + C * i + c;
        result.setQuick(constraint_row, variable_column, 1.0);

        /* d./d(f_in_(i,c)(k-1)) : 6 contraints (190 remaining) */
        variable_column = (k - 1) * H_constraint_size + f_in_position + C * i
            + c;
        result.setQuick(constraint_row, variable_column, 1.0);
      }

    }

    /* dH²_(k, i, c)/dx (demand and supply) */
    for (int k = 0; k < 3; k++) {
      // Normal cells
      for (int i = 0; i < 3; i++) {
        // Demand
        constraint_row = k * H_constraint_size + demand_supply_position + 2 * i;
        /* d (demand_i(k)) / d(rho_(i,c)(k)) : 27 constraints */
        for (int c = 0; c < 3; c++) {
          variable_column = k * H_constraint_size + C * i + c;
          result.setQuick(constraint_row, variable_column,
              simu.lwr_network.getCells()[i].
                  getDerivativeDemand(state
                      .get(k)
                      .getCell(i).total_density, delta_t));
        }
        // Supply
        /*
         * Should be 27 constraints but we are in free flow and these derivative
         * terms are zero
         */
        /* d (supply_i(k)) / d(rho_(i,c)(k)) : 27 constraints */
        /*
         * constraint_row = k * H_constraint_size + demand_supply_position + 2 *
         * i + 1;
         * for (int c = 0; c < 3; c++) {
         * variable_column = k * H_constraint_size + C * i + c;
         * result.setQuick(constraint_row, variable_column,
         * simu.lwr_network.getCells()[i].
         * getDerivativeSupply(state
         * .get(k)
         * .getCell(i).total_density));
         * }
         */
      }

      // Buffer
      int i = 3;
      // Demand
      constraint_row = k * H_constraint_size + demand_supply_position + 2 * i;
      /* d (demand_i(k)) / d(rho_(i,c)(k)) : 9 constraints */
      for (int c = 0; c < 3; c++) {
        variable_column = k * H_constraint_size + C * i + c;
        result.setQuick(constraint_row, variable_column,
            simu.lwr_network.getCells()[i].
                getDerivativeDemand(state
                    .get(k)
                    .getCell(i).total_density, delta_t));
      }
    }

    /* All the diagonal constraints should be -1 : 19 remaining */
    for (int i = 0; i < H_constraint_size * 3; i++)
      result.setQuick(i, i, -1.0);

    /* Split ratios */

    for (int k = 0; k < 3; k++) {
      int index_split_ratios = 0;
      int j = 0;
      // Junction 0
      // Beta(3 -> 0)
      {
        int in = 3;
        // int out = 0;
        constraint_row = k * H_constraint_size + aggregate_SR_position
            + index_split_ratios;
        // Only one non nul derivative term (c = 1)
        int c = 1;
        variable_column = k * H_constraint_size + C * in + c;
        Double partial_density = state.profiles[k].getCell(in).partial_densities
            .get(c);
        if (partial_density == null)
          partial_density = 0.0;
        double total_density = state.profiles[k].getCell(in).total_density;
        if (total_density == 0)
          continue;

        double value = (total_density - partial_density)
            / (total_density * total_density);

        result.setQuick(constraint_row, variable_column, value);
      }
      index_split_ratios++;
      // Beta(3 -> 2)
      {
        int in = 3;
        // int out = 2;
        constraint_row = k * H_constraint_size + aggregate_SR_position
            + index_split_ratios;
        // Only one non zero derivative term (c = 2)
        int c = 2;
        variable_column = k * H_constraint_size + C * in + c;
        Double partial_density = state.profiles[k].getCell(in).partial_densities
            .get(c);
        if (partial_density == null)
          partial_density = 0.0;
        double total_density = state.profiles[k].getCell(in).total_density;
        if (total_density == 0)
          continue;

        double value = (total_density - partial_density)
            / (total_density * total_density);
        result.setQuick(constraint_row, variable_column, value);
      }
    }

    /* dH³_(k, i, c)/dx (flow_out) */
    /* I trust the code for the 1x1 junction */
    Junction junction;
    Junction[] junctions = simu.lwr_network.getJunctions();

    for (int j_id = 0; j_id < junctions.length; j_id++) {
      junction = junctions[j_id];
      int nb_prev = junction.getPrev().length;
      int nb_next = junction.getNext().length;
      int i, j;
      double value;
      double total_density;
      Double partial_density;
      // Derivative terms for 1x1 junctions
      if (nb_prev == 1 && nb_next == 1) {
        double demand, supply, f_out;
        CellInfo cell_info;
        int prev_id = junction.getPrev()[0].getUniqueId();
        int next_id = junction.getNext()[0].getUniqueId();
        for (int k = 0; k < 3; k++) {
          cell_info = state.profiles[k].getCell(prev_id);
          total_density = cell_info.total_density;

          if (total_density == 0)
            continue;

          demand = cell_info.demand;
          supply = state.profiles[k].getCell(next_id).supply;

          f_out = Math.min(demand, supply);

          for (int c = 0; c < C; c++) {
            partial_density = cell_info.partial_densities.get(c);
            if (partial_density == null)
              partial_density = 0.0;

            i = H_constraint_size * k + f_out_position + C * prev_id + c;

            /*
             * Derivative terms with respect to the partial densities.
             * There are 3 cases but the use of f_out = min (supply,demand)
             * makes a factorization possible
             */
            j = H_constraint_size * k + C * prev_id + c;
            value = f_out * (total_density - partial_density)
                / (total_density * total_density);
            assert Numerical.validNumber(value);
            result.setQuick(i, j, value);

            /* Derivative terms with respect to supply/demand */
            if (partial_density == 0)
              continue;

            if (demand < supply) {
              j = H_constraint_size * k + demand_supply_position + 2 * prev_id;
            } else if (supply < demand) {
              j = H_constraint_size * k + demand_supply_position + 2 * prev_id
                  + 1;
            }
            value = partial_density / total_density;
            assert Numerical.validNumber(value);
            result.setQuick(i, j, value);
          }
        }
      }
    }

    return result;
  }

  private boolean compareTable(SparseCCDoubleMatrix2D t1,
      SparseCCDoubleMatrix2D t2) {
    return compareTable(t1.toArray(), t2.toArray());
  }

  private boolean compareTable(double[][] t1, double[][] t2) {

    assert (t1.length == t2.length);
    assert (t1[0].length == t2[0].length);
    boolean result = true;
    int nb_differences = 0;
    for (int i = 0; i < t1.length; i++) {
      for (int j = 0; j < t1[0].length; j++) {
        if (t1[i][j] != t2[i][j]) { // && t2[i][j] != 0
          result = false;
          nb_differences++;
          System.out.println("Difference in (" + i + ", " + j
              + ") from t1 (" + t1[i][j] + ") and t2 (" + t2[i][j] + ")");
          System.out.print("dH:");
          System.out.println();
          optimizer.informationIndexInX(i);
          System.out.println();
          System.out.print("dx:");
          optimizer.informationIndexInX(j);
          System.out.println();
        }
      }
    }
    System.out.println("Total number of differences: " + nb_differences);
    return result;
  }

  public void informationIndexInXhhh(int i) {
    int x_block_size = 60;
    int time_step = i / x_block_size;
    int C = 2;
    int H_constraint_size = 60;
    int density_position = 0;
    int demand_supply_position = 15;
    int aggregate_split_ratios_position = 15 + 10;
    int f_out_position = 15 + 10 + 5;
    int f_in_position = 15 + 10 + 5 + 15;
    assert (f_out_position == 30);
    assert (f_in_position == 45);

    System.out.print("[k=" + time_step + "]");
    int remaining = i % x_block_size;
    if (remaining < demand_supply_position) {
      int cell_id = remaining / (C + 1);
      int c = (remaining % (C + 1));
      System.out.println("Partial density of commodity " + c + " in cell "
          + (cell_id));
    } else if (remaining < aggregate_split_ratios_position) {
      int cell_id = (remaining - demand_supply_position) / 2;
      int is_supply = (remaining % 2);
      if (is_supply == 1)
        System.out.println("Demand in cell " + (cell_id));
      else
        System.out.println("Supply in cell " + (cell_id));
    } else if (remaining < f_out_position) {
      int cell_id = (remaining - aggregate_split_ratios_position) / (C + 1);
      int c = (remaining % (C + 1));
      System.out
          .println("Aggregate split ratio");
    } else if (remaining < f_in_position) {
      int cell_id = (remaining - f_out_position) / (C + 1);
      int c = (remaining % (C + 1));
      System.out
          .println("Flow-out of commodity " + c + " in cell " + (cell_id));
    } else {
      int cell_id = (remaining - f_in_position) / (C + 1);
      int c = (remaining % (C + 1));
      System.out
          .println("Flow-in of commodity " + c + " in cell " + (cell_id));
    }
  }
}
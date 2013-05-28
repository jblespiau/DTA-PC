package dta_solver;

import java.util.Iterator;
import java.util.Map.Entry;

import generalLWRNetwork.Cell;
import generalLWRNetwork.Origin;
import generalNetwork.state.CellInfo;
import generalNetwork.state.Profile;
import generalNetwork.state.externalSplitRatios.IntertemporalOriginsSplitRatios;

import org.apache.commons.math3.optimization.DifferentiableMultivariateOptimizer;
import org.wsj.AdjointForJava;

import scala.Option;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleFactory1D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;

public class SO_Optimizer extends AdjointForJava<DTA_ParallelSimulator> {

  private Simulator simulation;

  public SO_Optimizer(DifferentiableMultivariateOptimizer op, int maxIter,
      Simulator simu) {
    super(op, maxIter);
    simulation = simu;
  }

  public double[] getControl() {

    int T = simulation.time_discretization.getNb_steps();
    int C = simulation.lwr_network.getNb_commodities();
    Origin[] sources = simulation.lwr_network.getSources();
    IntertemporalOriginsSplitRatios splits = simulation.splits;
    int O = sources.length;

    /* For every time steps there is C compliant flows, and O non compliant */
    double[] control = new double[T * (C + O)];

    int index_in_control = 0;
    int commodity;
    double split_ratio;
    for (int orig = 0; orig < O; orig++) {
      for (int k = 0; k < T; k++) {
        /*
         * Mapping between
         * splits.get(sources[orig], k).get(0) and U[k*(C + sources.length)]
         */
        split_ratio = splits.get(sources[orig], k).get(0);
        index_in_control = k * (C + O);
        control[index_in_control] = split_ratio;
      }
      index_in_control++;

      Iterator<Integer> it = sources[orig]
          .getCompliant_commodities()
          .iterator();
      while (it.hasNext()) {
        commodity = it.next();
        for (int k = 0; k < T; k++) {
          /*
           * Mapping between
           * splits.get(sources[orig], k).get(commodity) and
           * U[k*(C +sources.length) + index_in_control]
           */
          split_ratio = splits.get(sources[orig], k).get(commodity);
          index_in_control = k * (C + O) + index_in_control;
          control[index_in_control] = split_ratio;
        }
        index_in_control++;
      }
    }

    return control;
  }

  public void parseStateVector(Profile p) {

    int T = simulation.time_discretization.getNb_steps();
    int C = simulation.lwr_network.getNb_commodities();
    Cell[] cells = simulation.lwr_network.getCells();

    /* Size of the description of a profile for a given time step */
    int block_size = (3 * (C + 1) + 2) * cells.length;
    /* Size of a block describing all the densities for a given time step */
    int size_density_block = cells.length * (C + 1);
    /* Size of a block describing all the supply/demand at one time step */
    int size_demand_suply_block = 2 * cells.length;
    /* Size of a block describing out-flows */
    int size_f_out_block = size_density_block;

    int block_id, sub_block_id;
    int commodity;
    int index_in_state = 0;
    double value;
    CellInfo cell_info;
    for (int k = 0; k < T; k++) {
      /* Id of the first data of time step k */
      block_id = k * block_size;

      for (int cell_id = 0; cell_id < cells.length; cell_id++) {

        cell_info = p.get(cells[cell_id]);
        /* Id of the first index containing data from cells[cell_id] */
        sub_block_id = block_id + cell_id * C;

        // Operations on densities
        Iterator<Entry<Integer, Double>> it =
            cell_info.partial_densities.entrySet().iterator();
        Entry<Integer, Double> entry;
        while (it.hasNext()) {
          entry = it.next();
          commodity = entry.getKey();
          // density (cell_id, commodity)(k)
          index_in_state = sub_block_id + commodity;
          value = entry.getValue();
        }

        // Operations on demand and supply
        index_in_state = sub_block_id + size_density_block;
        value = cell_info.demand;
        index_in_state++;
        value = cell_info.supply;

        // Operation on out-flows
        sub_block_id += size_demand_suply_block;
        it = cell_info.out_flows.entrySet().iterator();
        while (it.hasNext()) {
          entry = it.next();
          commodity = entry.getKey();
          // flow_out (cell_id, commodity)(k)
          index_in_state = sub_block_id + commodity;
          value = entry.getValue();
        }

        // Operations on in-flows
        index_in_state += size_f_out_block;
        it = cell_info.in_flows.entrySet().iterator();
        while (it.hasNext()) {
          entry = it.next();
          commodity = entry.getKey();
          // flow_in (cell_id, commodity)(k)
          index_in_state = sub_block_id + commodity;
          value = entry.getValue();
        }
      }
    }
  }

  @Override
  public Option<SparseCCDoubleMatrix2D> dhdu(DTA_ParallelSimulator arg0,
      double[] arg1) {
    // TODO Auto-generated method stub
    // // Some<Double> d = new Some<Double>(Double.valueOf(1));
    return null;
  }

  @Override
  public Option<SparseCCDoubleMatrix2D> dhdx(DTA_ParallelSimulator arg0,
      double[] arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DoubleMatrix1D djdu(DTA_ParallelSimulator arg0, double[] arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SparseDoubleMatrix1D djdx(DTA_ParallelSimulator arg0, double[] arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DTA_ParallelSimulator forwardSimulate(double[] arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double objective(DTA_ParallelSimulator arg0, double[] arg1) {
    // TODO Auto-generated method stub
    return 0;
  }
}

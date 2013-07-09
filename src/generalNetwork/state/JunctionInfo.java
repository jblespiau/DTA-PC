package generalNetwork.state;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.PairCells;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;

public class JunctionInfo {

  protected LinkedHashMap<PairCells, Double> aggregate_split_ratios;
  /*
   * Be careful that we use the fact that the default value is false. If you
   * want to change this, you will have to make sure the rest of the code is
   * still correct
   */
  protected boolean is_supply_limited = false;
  protected boolean is_demand_limited = false;
  protected HashMap<Integer, Double> flow_out;

  protected JunctionInfo(int prev) {
    flow_out = new HashMap<Integer, Double>(prev);
  }

  public JunctionInfo(int prev, int next) {
    aggregate_split_ratios = new LinkedHashMap<PairCells, Double>(prev * next);
    flow_out = new HashMap<Integer, Double>(prev);
  }

  public JunctionInfo(Junction j) {
    aggregate_split_ratios =
        new LinkedHashMap<PairCells, Double>(j.getPrev().length
            * j.getNext().length);
  }

  public Double getAggregateSR(Cell in, Cell out) {
    return aggregate_split_ratios.get(new PairCells(in, out));
  }

  public Double getAggregateSR(int in, int out) {
    return aggregate_split_ratios.get(new PairCells(in, out));
  }

  public void putAggregateSR(Cell in, Cell out, double value) {
    aggregate_split_ratios.put(new PairCells(in, out), new Double(value));
  }

  public void putAggregateSR(int in, int out, double value) {
    aggregate_split_ratios.put(new PairCells(in, out), new Double(value));
  }

  public Iterator<Entry<PairCells, Double>> entryIterator() {
    return aggregate_split_ratios.entrySet().iterator();
  }

  public void put(PairCells pair, double value) {
    aggregate_split_ratios.put(pair, value);
  }

  public double size() {
    return aggregate_split_ratios.size();
  }

  public boolean is_supply_limited() {
    return is_supply_limited;
  }

  public void set_supply_limited() {
    this.is_supply_limited = true;
  }

  public boolean is_demand_limited() {
    return is_demand_limited;
  }

  public void set_demand_limited() {
    this.is_demand_limited = true;
  }

  public void putFlowOut(Cell in, double value) {
    flow_out.put(in.getUniqueId(), value);
  }

  public void putFlowOut(int in_cell_id, double value) {
    flow_out.put(in_cell_id, value);
  }

  public double getFlowOut(int in_cell_id) {
    Double result = flow_out.get(in_cell_id);
    if (result == null)
      return 0.0;
    else
      return result.doubleValue();
  }
}
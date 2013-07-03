package generalNetwork.state;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.PairCells;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;

public class JunctionInfo {

  private LinkedHashMap<PairCells, Double> aggregate_split_ratios;

  public JunctionInfo(int prev, int next) {
    aggregate_split_ratios = new LinkedHashMap<PairCells, Double>(prev * next);
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
}
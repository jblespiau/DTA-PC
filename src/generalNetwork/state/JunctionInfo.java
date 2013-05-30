package generalNetwork.state;

import java.util.LinkedHashMap;

import dataStructures.PairCells;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;

public class JunctionInfo {

  public LinkedHashMap<PairCells, Double> aggregate_split_ratios;

  public JunctionInfo(int prev, int next) {
    aggregate_split_ratios = new LinkedHashMap<PairCells, Double>(prev * next);
  }

  public JunctionInfo(Junction j) {
    aggregate_split_ratios =
        new LinkedHashMap<PairCells, Double>(j.getPrev().length
            * j.getNext().length);
  }

  public double getAggregateSR(Cell in, Cell out) {
    Double res = aggregate_split_ratios.get(new PairCells(in, out));
    if (res == null) {
      System.out.println("Get a null result for an aggregate split ratio");
      System.exit(1);
    }
    return res;
  }

  public double getAggregateSR(int in, int out) {
    Double res = aggregate_split_ratios.get(new PairCells(in, out));
    if (res == null) {
      System.out.println("Get a null result for an aggregate split ratio");
      System.exit(1);
    }
    return res;
  }

  public void putAggregateSR(Cell in, Cell out, double value) {
    aggregate_split_ratios.put(new PairCells(in, out), value);
  }

  public void putAggregateSR(int in, int out, double value) {
    aggregate_split_ratios.put(new PairCells(in, out), value);
  }
}
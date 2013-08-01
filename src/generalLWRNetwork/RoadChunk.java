package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.Numerical;

/**
 * @class OrdinaryCell
 * @brief An OrdinaryCell represent a chunk of road that can be defined with a
 *        fundamental triangular diagram. Be aware that the maximum in-low and
 *        out-flow must be the same
 */
public class RoadChunk extends Cell {

  /* Constants */
  public double length, v, w;
  public double[] F_max, jam_density;
  private Junction next;

  /* Variables */
  public LinkedHashMap<Integer, Double> initial_densities;

  // private double supply_change;
  // private double demande_change;

  private void build(double l, double v, double w, double f_max,
      double jam_capacity, int nb_time_steps) {
    this.length = l;
    this.v = v;
    this.w = w;
    F_max = new double[nb_time_steps];
    jam_density = new double[nb_time_steps];
    for (int i = 0; i < F_max.length; i++) {
      F_max[i] = f_max;
      this.jam_density[i] = jam_capacity;
    }

  }

  public RoadChunk(double l, double v, double w, double f_max,
      double jam_capacity, int nb_time_steps) {
    super();
    build(l, v, w, f_max, jam_capacity, nb_time_steps);
    this.initial_densities = new LinkedHashMap<Integer, Double>();
  }

  public RoadChunk(double l, double v, double w, double f_max,
      double jam_capacity,
      LinkedHashMap<Integer, Double> initial_densities, int nb_time_steps) {
    super();
    build(l, v, w, f_max, jam_capacity, nb_time_steps);

    this.initial_densities = new LinkedHashMap<Integer, Double>(
        initial_densities);
  }

  /**
   * @brief Build a triangular diagram from only 3 data and the discretization
   * @param v
   * @param f_max
   * @param jam_capacity
   * @param delta_t
   */
  public RoadChunk(double v, double f_max, double jam_capacity, double delta_t,
      int nb_time_steps) {
    build(v * delta_t, v, v * f_max / (v * jam_capacity - f_max), f_max,
        jam_capacity, nb_time_steps);
    this.initial_densities = new LinkedHashMap<Integer, Double>();
  }

  public String detailstoString() {
    return "Cell: " + getUniqueId() + "\n" + "F_in=" + F_max + "\n"
        + "F_max=" + F_max + "\n" + "v=" + v + "\n" + "w=" + w + "\n"
        + "jam_density=" + jam_density;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Road" + "->J" + next.getUniqueId() + "]";
  }

  @Override
  public void print() {
    System.out.println(toString());
  }

  public boolean isCongested(double density, int time_step) {
    double supply_change = -F_max[time_step] / w + jam_density[time_step];
    return density > supply_change;
  }

  @Override
  public void setNext(Junction j) {
    next = j;
  }

  @Override
  public Junction getNext() {
    return next;
  }

  @Override
  public void checkConstraints(double delta_t) {
    int u_id = this.getUniqueId();

    assert next != null : "The roadChunck " + getUniqueId()
        + " has no following junction";

    Iterator<Double> densities = initial_densities.values().iterator();
    while (densities.hasNext()) {
      assert densities.next() >= 0 : "Cell " + u_id
          + "the initial density " + densities.next()
          + " must be greater than 0";
    }
    assert v <= length / delta_t : "Cell " + u_id + ": CLF condition " + v
        + " <= " + length + " / " + delta_t + " not respected";
    assert w <= length / delta_t : "Cell " + u_id + ": CLF condition " + w
        + " <= " + length + " / " + delta_t + " not respected";
    if (v != length / delta_t)
      System.out
          .println("Cell "
              + u_id
              + ": v != l / delta_t: you may have strange behaviour "
              + "v :"
              + v
              + "l :"
              + length
              + "delta_t"
              + delta_t
              + "v * delta_t"
              + (v * delta_t)
              + "(exponential decrease of the density in a emptying cell)");

    for (int i = 0; i < F_max.length; i++) {
      double supply_change = -F_max[i] / w + jam_density[i];
      double demande_change = F_max[i] / v;
      assert F_max[i] < w * jam_density[i] : "Cell " + u_id
          + ": We should have F_max < w * jam_density";

      assert demande_change <= supply_change : "Cell "
          + u_id
          + ": "
          + demande_change
          + "<="
          + supply_change
          + ": The density of free-flow should be smaller than the density of jammed flow.";
    }
  }

  @Override
  public double getDemand(double density, int time_step, double delta_t) {
    return Math.max(0, Math.min(F_max[time_step], v * density));
  }

  @Override
  public double getDerivativeDemand(double total_density, int time_step,
      double delta_t) {
    if (v * total_density < F_max[time_step]) {
      return v;
    } else {
      return 0.0;
    }
  }

  @Override
  public double getSupply(double density, int k) {
    return Math.max(0, Math.min(F_max[k], w * (jam_density[k] - density)));
  }

  @Override
  public double getDerivativeSupply(double total_density, int k) {
    if (F_max[k] < w * (jam_density[k] - total_density))
      return 0.0;
    else
      return -w;
  }

  @Override
  public LinkedHashMap<Integer, Double> getInitialDensity() {
    return initial_densities;
  }

  @Override
  public double getLength() {
    return length;
  }

  @Override
  public double getJamDensity(int time_step) {
    return jam_density[time_step];
  }

  @Override
  public LinkedHashMap<Integer, Double> getUpdatedDensity(
      LinkedHashMap<Integer, Double> densities,
      LinkedHashMap<Integer, Double> in_flows,
      LinkedHashMap<Integer, Double> out_flows, double delta_t) {

    LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();
    /*
     * To make it simple, first we add the densities in the result.
     * Then we add the in_flows, and then we remove the out_flows
     */

    Iterator<Entry<Integer, Double>> densities_it = densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (densities_it.hasNext()) {
      entry = densities_it.next();
      result.put(new Integer(entry.getKey()), new Double(entry.getValue()));
    }

    Iterator<Entry<Integer, Double>> iterator_in_flows = in_flows
        .entrySet().iterator();
    int commodity;
    Double in_flow;
    Double density;
    while (iterator_in_flows.hasNext()) {
      entry = iterator_in_flows.next();
      commodity = entry.getKey();
      in_flow = entry.getValue();
      density = result.get(commodity);

      if (density == null)
        density = 0.0;

      result.put(commodity, density + delta_t / length * in_flow);

    }

    Iterator<Entry<Integer, Double>> iterator_out_flows = out_flows
        .entrySet().iterator();
    Double out_flow;
    while (iterator_out_flows.hasNext()) {
      entry = iterator_out_flows.next();
      commodity = entry.getKey();
      out_flow = entry.getValue();
      density = result.get(commodity) - delta_t / length * out_flow;
      assert Numerical.greaterThan(density, 0, 10E-10) : "The density should be positive."
          +
          "Initial density: " + result.get(commodity) +
          " Out-flow: " + out_flow +
          " delta_t: " + delta_t +
          " gives a new density of " + density;
      if (density == 0)
        result.remove(commodity);
      else
        result.put(commodity, density);
    }

    return result;
  }

  @Override
  public boolean isSink() {
    return false;
  }

  @Override
  public boolean isBuffer() {
    return false;
  }
}
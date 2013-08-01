package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.Numerical;
import dataStructures.Preprocessor;

/**
 * @class OrdinaryCell
 * @brief An OrdinaryCell represent a chunk of road that can be defined with a
 *        fundamental triangular diagram. Be aware that the maximum in-low and
 *        out-flow must be the same
 */
public class RoadChunk extends Cell {

  /* Constants */
  public double length, v, w, F_max, jam_density;
  private Junction next;

  /* Variables */
  public LinkedHashMap<Integer, Double> initial_densities;
  private double supply_change;
  private double demande_change;

  private void build(double l, double v, double w, double f_max,
      double jam_capacity) {
    this.length = l;
    this.v = v;
    this.w = w;
    F_max = f_max;
    this.jam_density = jam_capacity;

    supply_change = -F_max / w + jam_density;
    demande_change = F_max / v;
  }

  public RoadChunk(double l, double v, double w, double f_max,
      double jam_capacity) {
    super();
    build(l, v, w, f_max, jam_capacity);
    this.initial_densities = new LinkedHashMap<Integer, Double>();
  }

  public RoadChunk(double l, double v, double w, double f_max,
      double jam_capacity,
      LinkedHashMap<Integer, Double> initial_densities) {
    super();
    build(l, v, w, f_max, jam_capacity);

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
  public RoadChunk(double v, double f_max, double jam_capacity, double delta_t) {
    build(v * delta_t, v, v * f_max / (v * jam_capacity - f_max), f_max,
        jam_capacity);
    this.initial_densities = new LinkedHashMap<Integer, Double>();
  }

  public String detailstoString() {
    return "Cell: " + getUniqueId() + "\n" + "F_in=" + F_max + "\n"
        + "F_max=" + F_max + "\n" + "v=" + v + "\n" + "w=" + w + "\n"
        + "jam_density=" + jam_density + "\n" + "\n" + "supply_change="
        + supply_change + "\n" + "demande_change=" + demande_change;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Road" + "->J" + next.getUniqueId() + "]";
  }

  @Override
  public void print() {
    System.out.println(toString());
  }

  public boolean isCongested(double density) {
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
    assert delta_t * v <= length : "Cell " + u_id + ": CLF condition " + v
        + " <= " + length + " / " + delta_t + " not respected";
    assert w * delta_t <= length : "Cell " + u_id + ": CLF condition " + w
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

    assert F_max < w * jam_density : "Cell " + u_id
        + ": We should have F_max < w * jam_density";

    assert demande_change <= supply_change : "Cell "
        + u_id
        + ": "
        + demande_change
        + "<="
        + supply_change
        + ": The density of free-flow should be smaller than the density of jammed flow.";
  }

  @Override
  public double getDemand(double density, double delta_t) {
    return Math.max(0, Math.min(F_max, v * density));
  }

  @Override
  public double getDerivativeDemand(double total_density, double delta_t) {
    if (v * total_density < F_max) {
      return v;
    } else {
      return 0.0;
    }
  }

  @Override
  public double getSupply(double density) {
    return Math.max(0, Math.min(F_max, w * (jam_density - density)));
  }

  @Override
  public double getDerivativeSupply(double total_density) {
    if (F_max < w * (jam_density - total_density))
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
  public double getJamDensity() {
    return jam_density;
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
      double value = density + delta_t / length * in_flow;
      if (value < 0) {
        if (Numerical.greaterThan(value, 0, 10E-10)) {
          if (Preprocessor.ZERO_ROUND_NOTIFICATION)
            System.out.println("[Notification] Negative partial density ("
                + value + ") rounded up to 0.");
          value = 0;
        } else {
          System.err.println("[Critical] Negative density: " + value
              + ". Aborting");
          System.exit(1);
        }
      }
      result.put(commodity, value);
    }

    Iterator<Entry<Integer, Double>> iterator_out_flows = out_flows
        .entrySet().iterator();
    Double out_flow;
    while (iterator_out_flows.hasNext()) {
      entry = iterator_out_flows.next();
      commodity = entry.getKey();
      out_flow = entry.getValue();
      double value = result.get(commodity) - delta_t / length * out_flow;

      if (value < 0) {
        if (Numerical.greaterThan(value, 0, 10E-10)) {
          if (Preprocessor.ZERO_ROUND_NOTIFICATION)
            System.out.println("[Notification] Negative partial density ("
                + value + ") rounded up to 0.");
          value = 0;
        } else {
          System.err.println("[Critical] Negative density: " + value
              + ". Aborting");
          System.exit(1);
        }
      }

      if (value == 0)
        result.remove(commodity);
      else
        result.put(commodity, value);
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
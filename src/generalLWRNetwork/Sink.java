package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @class Sink
 * @brief Represent a Sink with no bottleneck (supply is infinite)
 */
class Sink extends Cell {

  public Sink() {
    super();
  }

  /**
   * @brief Does nothing since there is no constraints on sinks
   */
  @Override
  public void checkConstraints(double delta_t) {
  }

  @Override
  public void setNext(Junction j) {
  }

  @Override
  public Junction getNext() {
    return null;
  }

  @Override
  public double getDemand(double density, int time_step, double delta_t) {
    return 0;
  }

  @Override
  public double getDerivativeDemand(double total_density, int time_step,
      double delta_t) {
    return 0;
  }

  @Override
  public double getSupply(double density, int time_step) {
    return Double.MAX_VALUE;
  }

  @Override
  public double getDerivativeSupply(double total_density, int time_step) {
    return 0;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Sink]";
  }

  @Override
  public void print() {
    System.out.println(toString());
  }

  @Override
  public LinkedHashMap<Integer, Double> getInitialDensity() {
    return new LinkedHashMap<Integer, Double>();
  }

  @Override
  public LinkedHashMap<Integer, Double> getUpdatedDensity(
      LinkedHashMap<Integer, Double> densities,
      LinkedHashMap<Integer, Double> in_flows,
      LinkedHashMap<Integer, Double> out_flows, double delta_t) {

    LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();

    /* We first add all the densities */
    Iterator<Entry<Integer, Double>> densities_it = densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (densities_it.hasNext()) {
      entry = densities_it.next();
      result.put(new Integer(entry.getKey()), new Double(entry.getValue()));
    }

    /* Then we add the in_flows */
    Iterator<Entry<Integer, Double>> iterator_in_flows = in_flows
        .entrySet().iterator();
    Entry<Integer, Double> f_in;
    int commodity;
    double in_flow;
    Double density;
    while (iterator_in_flows.hasNext()) {
      f_in = iterator_in_flows.next();
      commodity = f_in.getKey();
      in_flow = f_in.getValue();

      density = result.get(commodity);
      if (density == null) {
        density = delta_t * in_flow;
      } else {
        density = density + delta_t * in_flow;
      }
      assert density >= 0 : "Zero density in a buffer";

      result.put(commodity, density);
    }
    return result;
  }

  @Override
  public boolean isSink() {
    return true;
  }

  @Override
  public double getLength() {
    return 1.0;
  }

  @Override
  public double getJamDensity(int time_step) {
    assert (false);
    return Double.MAX_VALUE;
  }

  @Override
  public boolean isBuffer() {
    return false;
  }
}
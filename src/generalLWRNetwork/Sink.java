package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.Numerical;
import dataStructures.Preprocessor;

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
      double value;
      if (density == null) {
        value = delta_t * in_flow;
      } else {
        value = density + delta_t * in_flow;
      }

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

      assert value >= 0 : "Negative density(" + value + ") in a sink";

      result.put(commodity, value);
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
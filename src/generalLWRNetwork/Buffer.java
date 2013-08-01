package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import dataStructures.Numerical;
import dataStructures.Preprocessor;

/**
 * @class EntryCell
 * @brief An EntryCell contains a buffer and an OrdinaryCell. The buffer holds
 *        the cars wanting to go to the cell but which can't because of the
 *        limited in-flow
 * 
 */
public class Buffer extends Cell {

  private Junction next;

  public Buffer() {
    super();
  }

  public Buffer(Junction next) {
    super();
    this.next = next;
  }

  @Override
  public void checkConstraints(double delta_t) {
    assert next != null : "The buffer " + getUniqueId()
        + " has no following cell.";
  }

  @Override
  public double getDemand(double density, double delta_t) {
    return density / delta_t;
  }

  @Override
  public double getDerivativeDemand(double total_density, double delta_t) {
    return 1.0 / delta_t;
  }

  @Override
  public double getSupply(double density) {
    return 0;
  }

  @Override
  public double getDerivativeSupply(double total_density) {
    return 0;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Buffer->J" + next.getUniqueId() + "]";
  }

  @Override
  public void print() {
    System.out.println(toString());
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
  public LinkedHashMap<Integer, Double> getUpdatedDensity(
      LinkedHashMap<Integer, Double> densities,
      LinkedHashMap<Integer, Double> in_flows,
      LinkedHashMap<Integer, Double> out_flows, double delta_t) {

    assert in_flows == null || in_flows.size() == 0 : "There should not be any in-flow in a buffer";

    LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();

    /* We first add all the densities */
    Iterator<Entry<Integer, Double>> densities_it = densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (densities_it.hasNext()) {
      entry = densities_it.next();
      // TODO: remove this test
      if (entry.getValue() != 0)
        result.put(new Integer(entry.getKey()), new Double(entry.getValue()));
    }

    /* We update the densities for the flow that have out_flows */
    Iterator<Entry<Integer, Double>> iterator_out_flows =
        out_flows.entrySet().iterator();
    Entry<Integer, Double> f_out;
    int commodity;
    double out_flow;
    Double density;
    while (iterator_out_flows.hasNext()) {
      f_out = iterator_out_flows.next();
      commodity = f_out.getKey();
      out_flow = f_out.getValue();

      density = result.get(commodity);

      assert density != null || out_flow == 0 : "In the buffer, the density of an exiting commodity should not be null";
      // TODO: remove this test
      if (density == null && out_flow == 0)
        continue;

      double value = density - delta_t * out_flow;

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
      assert value >= 0 : "Negative density(" + value + ") in a buffer";

      if (density != 0) {
        result.put(commodity, value);
      } else {
        result.remove(commodity);
      }
    }
    return result;
  }

  @Override
  public LinkedHashMap<Integer, Double> getInitialDensity() {
    return new LinkedHashMap<Integer, Double>();
  }

  @Override
  public boolean isSink() {
    return false;
  }

  @Override
  public double getLength() {
    return 1.0;
  }

  @Override
  public double getJamDensity() {
    assert (false);
    return Double.MAX_VALUE;
  }

  @Override
  public boolean isBuffer() {
    return true;
  }
}
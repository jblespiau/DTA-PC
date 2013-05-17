package generalLWRNetwork;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
  public double getDemand(double density) {
    return density;
  }

  @Override
  public double getSupply(double density) {
    return 0;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Buffer]";
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
      assert density != null : "In the buffer, the density of an existing commodity should not be null";

      density = density - delta_t * out_flow;
      assert density >= 0 : "Zero density in a buffer";

      if (density != 0) {
        result.put(commodity, density);
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

}

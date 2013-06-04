package generalLWRNetwork;

import java.util.LinkedHashMap;

/**
 * @brief All the cells composing the networks have to implement some mandatory
 *        functions
 */
public abstract class Cell {
  private int unique_id;

  public Cell() {
    unique_id = NetworkUIDFactory.getId_cell();
  }

  public int getUniqueId() {
    return unique_id;
  }

  /*
   * ....._______...._______
   * ..../..................\
   * .../....................\
   * ../......................\
   * ----Demand ---- Supply
   */

  abstract public boolean isSink();

  abstract public String toString();

  public abstract void print();

  abstract public void setNext(Junction j);

  abstract public Junction getNext();

  abstract public LinkedHashMap<Integer, Double> getInitialDensity();

  abstract public double getLength();

  abstract public double getDemand(double total_density, double delta_t);

  abstract public double getDerivativeDemand(double total_density);;

  abstract public double getSupply(double total_density);

  abstract public double getDerivativeSupply(double total_density);

  abstract public LinkedHashMap<Integer, Double> getUpdatedDensity(
      LinkedHashMap<Integer, Double> densities,
      LinkedHashMap<Integer, Double> in_flows,
      LinkedHashMap<Integer, Double> out_flows, double delta_t);

  /**
   * @brief Checks the Courant–Friedrichs–Lewy conditions to be sure the
   *        discretization is ok
   */
  public abstract void checkConstraints(double delta_t);

  public boolean equals(Cell obj) {
    return unique_id == obj.getUniqueId();
  }

  @Override
  public int hashCode() {
    return getUniqueId();
  }

}
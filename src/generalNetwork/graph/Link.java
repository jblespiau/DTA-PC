package generalNetwork.graph;

import com.google.gson.annotations.Expose;

/**
 * @brief A link is the macroscopic representation of a road between two
 *        macroscopic intersections
 */
public class Link {

  @Expose
  protected int unique_id;
  @Expose
  public double l, v, w, F_max, jam_density;
  public transient Node from, to;
  public transient double initial_density;

  public Link(double l, double v, double w, double f_max, double jam_density,
      GraphUIDFactory id) {
    super();
    this.l = l;
    this.v = v;
    this.w = w;
    F_max = f_max;
    this.jam_density = jam_density;
    unique_id = id.getId_link();
  }

  void print() {
    System.out.println("[Link " + unique_id + ": " + from.unique_id + "->"
        + to.unique_id + "]");
    to.print();
  }

  public int getUnique_id() {
    return unique_id;
  }

  @Override
  public String toString() {
    return "Link [(" + unique_id +
        "): J" + from.getUnique_id() + " -> J" + to.getUnique_id() + "]";
  }
}
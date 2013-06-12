package generalNetwork.graph;

import com.google.gson.annotations.Expose;

public class GraphDestination {

  @Expose
  public int id;
  @Expose
  public String type;

  public GraphDestination(int id, String type) {
    super();
    this.id = id;
    this.type = type;
  }
}
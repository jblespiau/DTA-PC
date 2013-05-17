package generalNetwork.graph;

import com.google.gson.annotations.Expose;

public class Destination {

  @Expose
  public int id;
  @Expose
  public String type;

  public Destination(int id, String type) {
    super();
    this.id = id;
    this.type = type;
  }
}

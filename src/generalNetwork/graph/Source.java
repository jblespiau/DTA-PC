package generalNetwork.graph;

import com.google.gson.annotations.Expose;

public class Source {

  @Expose
  public int id;
  @Expose
  public String type;

  public Source(int id, String type) {
    super();
    this.id = id;
    this.type = type;
  }

  public void print() {
    System.out.println("[" + id + " :" + type + " origin]");
  }
}

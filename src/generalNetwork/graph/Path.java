package generalNetwork.graph;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.annotations.Expose;

public class Path {

  @Expose
  int unique_id;
  /* int[] path should NEVER be used except for serialization */
  @Expose
  private int[] path;
  ArrayList<Integer> path_list;

  public Path(GraphUIDFactory id) {
    unique_id = id.getId_Path();
    path_list = new ArrayList<Integer>();
  }

  public Path(ArrayList<Integer> l) {
    path_list = l;
  }

  void buildFromJson() {
    path_list = new ArrayList<Integer>(path.length);
    for (int i = 0; i < path.length; i++) {
      path_list.add(path[i]);
    }
  }

  void buildToJson() {
    path = new int[path_list.size()];
    for (int i = 0; i < path.length; i++) {
      path[i] = path_list.get(i);
    }
  }

  public Integer getFirstLink() {
    return path_list.get(0);
  }

  public Iterator<Integer> iterator() {
    return path_list.iterator();
  }

  public int getUnique_id() {
    return unique_id;
  }
}

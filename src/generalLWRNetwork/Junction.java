package generalLWRNetwork;

/**
 * @brief Represent a junctions between cells
 * @details For now we only accept 1*1 junctions
 * 
 */
public class Junction {

  private int unique_id;
  private Cell[] prev, next;

  public Junction() {
    unique_id = NetworkUIDFactory.getId_junctions();
    prev = null;
    next = null;
  }

  Junction(Cell[] predecessor, Cell[] successor) {
    unique_id = NetworkUIDFactory.getId_junctions();
    this.prev = predecessor.clone();
    this.next = successor.clone();
  }

  public int getUniqueId() {
    return unique_id;
  }

  public boolean isMergingJunction() {
    return next.length == 1;
  }

  public Cell[] getPrev() {
    return prev;
  }

  public void setPrev(Cell[] prev) {
    this.prev = prev.clone();
  }

  public Cell[] getNext() {
    return next;
  }

  public void setNext(Cell[] next) {
    this.next = next.clone();
  }

  public void addPrev(Cell c) {
    int i = 0;
    while (prev[i] != null)
      i++;
    prev[i] = c;
  }

  @Override
  public String toString() {
    String incells = "";
    for (int i = 0; i < prev.length; i++) {
      incells += prev[i].getUniqueId() + ",";
    }
    incells = "[" + incells + "]";

    String outcells = "";
    for (int i = 0; i < next.length; i++) {
      outcells += next[i].getUniqueId() + ",";
    }
    outcells = "[" + outcells + "]";
    return "[(" + unique_id + ")" + incells + "->" + outcells + "]";
  }

  public void checkConstraints() {
    assert (prev.length <= 1) : "Only junctions with less than 1 incoming link is working";
    assert (next.length <= 1) : "Only junctions with less than 1 outgoing link is working";
  }

  public void print() {
    System.out.println(toString());
  }
}
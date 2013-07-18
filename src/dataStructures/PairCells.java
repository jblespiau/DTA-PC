package dataStructures;

import generalLWRNetwork.Cell;

public class PairCells {

  public int incoming, outgoing;

  public PairCells(int incoming, int outgoing) {
    this.incoming = incoming;
    this.outgoing = outgoing;
  }

  public PairCells(Cell incoming, Cell outgoing) {
    this.incoming = incoming.getUniqueId();
    this.outgoing = outgoing.getUniqueId();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + incoming;
    result = prime * result + outgoing;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PairCells other = (PairCells) obj;
    if (incoming != other.incoming)
      return false;
    if (outgoing != other.outgoing)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "(incoming=" + incoming + ", outgoing=" + outgoing + ")";
  }
}
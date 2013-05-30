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
}
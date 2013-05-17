package generalLWRNetwork;

import java.util.LinkedList;

class OriginMultipleBuffer extends Origin {

  /* Needed to know how to split the demand in the buffers */
  // private IntertemporalJunctionSplitRatios repartition;

  public OriginMultipleBuffer(Junction j, String type,
      LinkedList<Cell> new_cells, LinkedList<Junction> new_junctions) {

    assert j.getNext() != null;

    int nb_outgoing_links = j.getNext().length;
    assert nb_outgoing_links > 0 : "The origin has no outgoing links";

    entries = new Buffer[nb_outgoing_links];

    Buffer b;
    Junction tmp;
    for (int i = 0; i < nb_outgoing_links - 1; i++) {
      b = new Buffer();
      new_cells.add(b);
      entries[i] = b;

      tmp = new Junction(new Cell[] { b }, new Cell[] { j.getNext()[i] });
      new_junctions.add(tmp);

    }
    // We transform the junction in a 1x1 junction for the last road
    b = new Buffer();
    new_cells.add(b);
    j.setNext(new Cell[] { j.getNext()[nb_outgoing_links - 1] });
    j.setPrev(new Cell[] { b });
  }

}

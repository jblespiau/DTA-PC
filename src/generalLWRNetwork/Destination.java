package generalLWRNetwork;

import java.util.LinkedList;

public class Destination {

  Sink sink;

  public Destination(Junction j, String type, LinkedList<Cell> new_cells,
      LinkedList<Junction> new_junctions) {
    assert j != null;
    assert type != null;
    assert j.getNext() == null : "A destination junction should have no outgoing links";

    if (type.equals("SingleJunction")) {
      // System.out.println("[Destination.java]Creation of a SingleJunction destination");

      int nb_incoming_links = j.getPrev().length;
      assert nb_incoming_links > 0 : "The destination has no incoming links";

      sink = new Sink();
      new_cells.add(sink);

      Cell cell;
      Junction tmp;
      for (int i = 0; i < nb_incoming_links - 1; i++) {
        cell = j.getPrev()[i];

        tmp = new Junction(new Cell[] { cell }, new Cell[] { sink });
        new_junctions.add(tmp);

        cell.setNext(tmp);
      }
      // We transform the junction in a 1x1 junction for the last road
      cell = j.getPrev()[nb_incoming_links - 1];
      cell.setNext(j); // Already true
      j.setNext(new Cell[] { sink });
      j.setPrev(new Cell[] { cell });

    } else {
      System.out.println("[Destination.java]Creation of a " + type
          + " destination. This type does not exist");
      System.exit(1);
    }
  }

}

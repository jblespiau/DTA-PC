package generalLWRNetwork;

import java.util.LinkedList;

class OriginSingleBuffer extends Origin {

  public OriginSingleBuffer(Junction j, String type,
      LinkedList<Cell> new_cells, LinkedList<Junction> new_junctions) {

    entries = new Buffer[1];
    Buffer b = new Buffer();
    entries[0] = b;
    new_cells.add(b);

    j.setPrev(new Cell[] { b });
    junction = j;
  }

}

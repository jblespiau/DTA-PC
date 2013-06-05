package generalLWRNetwork;

import generalNetwork.graph.Graph;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;
import generalNetwork.graph.Path;
import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class DiscretizedGraph {

  // TODO: turn everything in private
  /*
   * Contains the junctions which were not present in the graph added
   * because of the discretization
   */
  LinkedList<Junction> new_junctions;
  /* Contains all the cells created */
  LinkedList<Cell> new_cells;
  /*
   * link_to_cells[i] contains the head and tail cell of the discrete version
   * of a given link
   */
  private LinkPair[] link_to_cells;
  /* junctions[i] contains the junctions representing nodes[i] */
  Junction[] junctions;

  /* Maps node_id -> Origin */
  public HashMap<Integer, Origin> node_to_origin;
  /* Contains the sources */
  public Origin[] sources;
  /* Contains the destinations */
  Destination[] destinations;
  int total_nb_junctions = 0, total_nb_cells = 0;

  public IntertemporalSplitRatios split_ratios;
  int nb_paths;

  public DiscretizedGraph(Graph g, double delta_t, int time_steps) {
    new_cells = new LinkedList<Cell>();
    new_junctions = new LinkedList<Junction>();
    node_to_origin = new HashMap<Integer, Origin>();

    /* Reset the unique id generators for cells and junctions */
    NetworkUIDFactory.resetCell_id();
    NetworkUIDFactory.resetJunction_id();

    /* Discretize all the links */
    Link[] links = g.getLinks();
    link_to_cells = new LinkPair[links.length];
    for (int i = 0; i < links.length; i++) {
      link_to_cells[i] = discretizeLink(links[i], delta_t);
    }

    /* Transform Nodes into Junctions */
    Node[] nodes = g.getNodes();
    g.check();
    junctions = new Junction[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      junctions[i] = discretizeJunction(nodes[i], delta_t);
    }

    /*
     * We transform each origin node into an origin junction of the
     * corresponding type
     */
    int nb_origins = g.getOrigins().length;
    sources = new Origin[nb_origins];
    for (int o = 0; o < nb_origins; o++) {
      sources[o] = new Origin(junctions[g.getOrigins()[o].id],
          g.getOrigins()[o].type,
          new_cells, new_junctions);
      node_to_origin.put(g.getOrigins()[o].id, sources[o]);
    }

    /*
     * We transform every destination node into a destination junction of the
     * corresponding type
     */
    int nb_destinations = g.getDestinations().length;
    destinations = new Destination[nb_destinations];
    for (int d = 0; d < nb_destinations; d++) {
      destinations[d] = new Destination(junctions[g.getDestinations()[d].id],
          g.getDestinations()[d].type, new_cells, new_junctions);
    }

    /*
     * We check that all junctions are valid junctions
     */
    for (int i = 0; i < junctions.length; i++) {
      assert junctions[i].getPrev() != null : "Junction "
          + junctions[i].getUniqueId()
          + " has a null prev array. Your network is likely to be wrong.";
      assert junctions[i].getNext() != null : "Junction "
          + junctions[i].getUniqueId()
          + " has a null next array. Your network is likely to be wrong.";
    }
    /*
     * We add the compliant split_ratios corresponding to the different paths
     */
    split_ratios = new IntertemporalSplitRatios(junctions, time_steps);

    /*
     * We need to know the commodities leaving each origins to be able
     * to initialize default split ratios. It is when creating the commodities
     * according to paths that we create this mapping
     */
    HashMap<Junction, LinkedList<Integer>> commodities_at_origins =
        new HashMap<Junction, LinkedList<Integer>>(sources.length);

    Path[] paths = g.getPaths();
    for (int i = 0; i < paths.length; i++) {
      createSplitRatios(g, paths[i], commodities_at_origins);
    }
    nb_paths = paths.length;

    total_nb_junctions = NetworkUIDFactory.IdJunction() + 1;
    total_nb_cells = NetworkUIDFactory.IdCell() + 1;
  }

  /**
   * @brief Take a link and returns the (head, tail) cells of the discretized
   *        link
   * @details It add 1x1 junctions between the cells and all those junctions are
   *          added in new_junctions
   */
  private LinkPair discretizeLink(Link link, double delta_t) {
    LinkPair result = new LinkPair();

    double length = link.l;
    double v = link.v;
    double w = link.w;
    double jam_density = link.jam_density;
    double F_max = link.F_max;

    int nb_cell_to_build = (int) Math.ceil(length / (v * delta_t));
    assert nb_cell_to_build > 0 : "We must build a > 0 number of cells";

    // We build the following links
    Cell cell;
    Junction current_j, previous_j = null;
    int i;
    for (i = 0; i < nb_cell_to_build - 1; i++) {
      cell = new RoadChunk(v * delta_t, v, w, F_max, jam_density);
      new_cells.add(cell);
      if (i == 0) {
        result.begin = cell;
      }
      if (previous_j != null) {
        previous_j.setNext(new Cell[] { cell });
      }
      current_j = new Junction();
      new_junctions.add(current_j);

      current_j.setPrev(new Cell[] { cell });
      cell.setNext(current_j);
      previous_j = current_j;
    }

    // Last cell (or unique cell) of the link
    cell = new RoadChunk(v * delta_t, v, w, F_max, jam_density);
    new_cells.add(cell);
    if (i == 0) {
      result.begin = cell;
    }
    result.end = cell;
    if (previous_j != null)
      previous_j.setNext(new Cell[] { cell });

    return result;
  }

  /**
   * @brief Take a node and returns the equivalent junction
   * @details It need link_to_cell to have been initialized
   * @return
   */
  private Junction discretizeJunction(Node node, double delta_t) {
    Junction result = new Junction();

    int nb_incoming = node.incoming_links.size();
    int nb_outgoing = node.outgoing_links.size();
    Cell[] incoming = new Cell[nb_incoming];
    Cell[] outgoing = new Cell[nb_outgoing];
    for (int i = 0; i < nb_incoming; i++) {
      incoming[i] = lastCellofLink(node.incoming_links.get(i).getUnique_id());
    }
    for (int i = 0; i < nb_outgoing; i++) {
      outgoing[i] = firstCellofLink(node.outgoing_links.get(i).getUnique_id());
    }

    if (nb_incoming != 0)
      result.setPrev(incoming);
    if (nb_outgoing != 0)
      result.setNext(outgoing);

    /*
     * If we have a NxM node with N > M > 0, we have to take into account the
     * priorities
     */
    if (nb_outgoing != 0 && (nb_incoming > nb_outgoing)) {
      HashMap<Integer, Double> priorities = new HashMap<Integer, Double>(
          nb_incoming);

      Iterator<Entry<Integer, Double>> it =
          node.priorities.entrySet().iterator();
      Entry<Integer, Double> entry;

      while (it.hasNext()) {
        entry = it.next();
        priorities.put(lastCellofLink(entry.getKey()).getUniqueId(),
            entry.getValue());
      }

      result.setPriorities(priorities);
    }

    return result;
  }

  /**
   * @brief Build the split ratios for the compliant commodities
   */
  private void createSplitRatios(Graph g, Path p,
      HashMap<Junction, LinkedList<Integer>> commodities_at_origins) {

    Iterator<Integer> iterator = p.iterator();
    Link[] links = g.getLinks();
    Junction j;
    int previous_link_id = -1, current_link_id = -1;
    LinkedList<Integer> commodities;
    while (iterator.hasNext()) {
      current_link_id = iterator.next();

      j = junctions[links[current_link_id].from.getUnique_id()];
      assert j != null;

      // We add one commodity at the junction
      commodities = commodities_at_origins.get(j);
      if (commodities == null) {
        commodities = new LinkedList<Integer>();
        commodities_at_origins.put(j, commodities);
      }
      commodities.add(current_link_id + 1);

      // There is nothing to do for nx1 junctions
      if (!j.isMergingJunction()) {
        // We add a split ratio for the first junction (origin)
        // We have to take the id of the buffer placed before the junction
        if (previous_link_id == -1) {
          assert j.getPrev().length == 1 : "A multiple exit origin must have an incoming link";
          split_ratios.addCompliantSRToJunction(
              j.getPrev()[0].getUniqueId(),
              link_to_cells[current_link_id].begin.getUniqueId(),
              p.getUnique_id() + 1, 1, j);
        } else {
          split_ratios.addCompliantSRToJunction(
              link_to_cells[previous_link_id].end.getUniqueId(),
              link_to_cells[current_link_id].begin.getUniqueId(),
              p.getUnique_id() + 1, 1, j);
        }
      }

      previous_link_id = current_link_id;

    }

    /*
     * We say to each origins how many commodities will leave from them
     */
    LinkedList<Integer> tmp;
    for (int o = 0; o < sources.length; o++) {
      tmp = commodities_at_origins.get(sources[o].junction);
      if (tmp != null) {
        sources[o].compliant_commodities = tmp;
      } else {
        assert false;
      }
    }

    /*
     * There is nothing to do for the last node. However we check that the last
     * node is Nx1 junction
     */
    assert (junctions[links[current_link_id].to.getUnique_id()].getNext().length <= 1) : "The arrival of a path should not have multiple exits";
  }

  /**
   * @brief Give the junction created from the node of the given id
   * @param node_id
   *          The id of a node in the general graph
   * @return The junction that corresponds to this node in the discretized
   *         version
   */
  public Junction nodeToJunction(int node_id) {
    return junctions[node_id];
  }

  /**
   * @brief Returns the first cell of the equivalent representation of the link
   *        of the given id
   * @param link_id
   *          The id of the link in the general graph
   * @return The first cell of the discretized version of the link
   */
  public Cell firstCellofLink(int link_id) {
    return link_to_cells[link_id].begin;
  }

  public Cell firstCellofLink(Link l) {
    return firstCellofLink(l.getUnique_id());
  }
  /**
   * @brief Returns the last cell of the equivalent representation of the link
   *        of the given id
   * @param link_id
   *          The id of the link in the general graph
   * @return The last cell of the discretized version of the link
   */
  public Cell lastCellofLink(int link_id) {
    return link_to_cells[link_id].end;
  }
  
  public Cell lastCellofLink(Link l) {
    return lastCellofLink(l.getUnique_id());
  }
}
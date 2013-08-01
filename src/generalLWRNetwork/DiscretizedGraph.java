package generalLWRNetwork;

import generalNetwork.graph.Graph;
import generalNetwork.graph.GraphDestination;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;
import generalNetwork.graph.Path;
import generalNetwork.graph.Source;
import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class DiscretizedGraph {

  // TODO: turn everything in private
  public Node[] graph_nodes; /* We suppose nodes[i].unique_id = i */
  public Link[] graph_links; /* We suppose links[i].unique_id = i */
  public Source[] graph_origins;
  public GraphDestination[] graph_destinations;
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
    graph_nodes = g.getNodes();
    graph_links = g.getLinks();
    graph_origins = g.getOrigins();
    graph_destinations = g.getDestinations();

    new_cells = new LinkedList<Cell>();
    new_junctions = new LinkedList<Junction>();
    node_to_origin = new HashMap<Integer, Origin>();

    /* Reset the unique id generators for cells and junctions */
    NetworkUIDFactory.resetCell_id();
    NetworkUIDFactory.resetJunction_id();

    /* Discretize all the links */
    link_to_cells = new LinkPair[graph_links.length];
    for (int i = 0; i < graph_links.length; i++) {
      link_to_cells[i] = discretizeLink(graph_links[i], time_steps, delta_t);
    }

    /* Transform Nodes into Junctions */
    Node[] nodes = g.getNodes();
    g.check();
    junctions = new Junction[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      junctions[i] = discretizeJunction(nodes[i], delta_t);
      /* We set the next junction of the last cells of a link */
      if (nodes[i].incoming_links.size() != 0) {
        for (int cp = 0; cp < nodes[i].incoming_links.size(); cp++) {
          lastCellofLink(nodes[i].incoming_links.get(cp)).setNext(junctions[i]);
        }
      }
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
    /* We check that all the cells except sinks have a next cell */
    Iterator<Cell> it = new_cells.iterator();
    Cell cell;
    while (it.hasNext())
    {
      cell = it.next();
      assert (cell.isSink() || cell.getNext() != null) : "Cell "
          + cell.getUniqueId() + " has no next junction";
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
    Path[] paths = g.getPaths();

    createSplitRatios(g, paths);

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
  private LinkPair discretizeLink(Link link, int nb_time_steps, double delta_t) {
    LinkPair result = new LinkPair();

    double length = link.l;
    double v = link.v;
    double w = link.w;
    double jam_density = link.jam_density;
    double F_max = link.F_max;

    int nb_cell_to_build = (int) Math.ceil(length / (v * delta_t));
    assert nb_cell_to_build > 0 : "We must build a > 0 number of cells";

    // We build the following links
    Cell cell = null;
    Junction current_j = null, previous_j = null;
    int i;
    for (i = 0; i < nb_cell_to_build - 1; i++) {
      cell = new RoadChunk(v * delta_t, v, w, F_max, jam_density, nb_time_steps);
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
      previous_j = current_j;
      cell.setNext(current_j);
    }
    if (cell != null)
      cell.setNext(current_j);

    // Last cell (or unique cell) of the link
    cell = new RoadChunk(v * delta_t, v, w, F_max, jam_density, nb_time_steps);
    new_cells.add(cell);
    if (i == 0) {
      result.begin = cell;
    }
    if (previous_j != null)
      previous_j.setNext(new Cell[] { cell });

    result.end = cell;

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
   * @details For every path, we have to add the id of the commodity at the
   *          origin (the junction from which the first link of the path is
   *          leaving). We also have to add the split ratios at junction crossed
   *          by the path where there is more than 1 outgoing link.
   */
  private void createSplitRatios(Graph g, Path[] paths) {

    /* paths[c] is used by commodity c+1 */
    for (int c = 0; c < paths.length; c++) {
      Path p = paths[c];
      assert p.getUnique_id() == c;
      Iterator<Integer> iterator = p.iterator();
      int previous_link_id = -1, current_link_id = -1;

      /* Origin */
      int origin_id = graph_links[p.getFirstLink()].from.getUnique_id();
      Origin o = node_to_origin.get(origin_id);

      /*
       * We add one commodity at the origin.
       * If it is the first, we create the linkedlist
       */
      LinkedList<Integer> commodities_at_origin = o.getCompliant_commodities();
      if (commodities_at_origin == null) {
        commodities_at_origin = new LinkedList<Integer>();
        o.compliant_commodities = commodities_at_origin;
      }
      commodities_at_origin.add(c + 1);

      while (iterator.hasNext()) {
        current_link_id = iterator.next();

        /* junction at the origin of the current_link */
        Junction j = junctions[graph_links[current_link_id].from.getUnique_id()];

        assert j != null;

        // There is nothing to do for nx1 junctions
        if (!j.isMergingJunction()) {
          // We add a split ratio for the first junction (origin)
          // We have to take the id of the buffer placed before the junction
          if (previous_link_id == -1) {
            assert j.getPrev().length == 1 : "The first junction must have an incoming link";
            split_ratios.addCompliantSRToJunction(
                j.getPrev()[0].getUniqueId(),
                link_to_cells[current_link_id].begin.getUniqueId(),
                c + 1, 1, j);
          } else {
            split_ratios.addCompliantSRToJunction(
                link_to_cells[previous_link_id].end.getUniqueId(),
                link_to_cells[current_link_id].begin.getUniqueId(),
                c + 1, 1, j);
          }
        }
        previous_link_id = current_link_id;
      }

      /*
       * There is nothing to do for the last node. However we check that the
       * last node is Nx1 junction
       */
      assert (junctions[graph_links[current_link_id].to.getUnique_id()]
          .getNext().length <= 1) : "The arrival of a path should not have multiple exits";
    }
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
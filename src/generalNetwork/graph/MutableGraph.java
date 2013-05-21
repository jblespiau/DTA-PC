package generalNetwork.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import generalNetwork.graph.Destination;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;
import generalNetwork.graph.Source;

public class MutableGraph {

  private GraphUIDFactory id_factory;
  Vector<Node> nodes;
  Vector<Link> links;

  Vector<Path> paths;
  Vector<Source> origins;
  Vector<Destination> destinations;

  /**
   * @brief Create a mutable graph from a Graph
   */
  public MutableGraph(Graph g) {

    nodes = new Vector<Node>(g.nodes.length);
    nodes.addAll(Arrays.asList(g.nodes));

    links = new Vector<Link>(g.links.length);
    links.addAll(Arrays.asList(g.links));

    paths = new Vector<Path>(g.paths.length);
    paths.addAll(Arrays.asList(g.paths));

    origins = new Vector<Source>(g.origins.length);
    origins.addAll(Arrays.asList(g.origins));

    destinations = new Vector<Destination>(g.destinations.length);
    destinations.addAll(Arrays.asList(g.destinations));

    id_factory = new GraphUIDFactory(g.links.length, g.nodes.length, g.paths.length);
  }

  public MutableGraph(String name) {

    id_factory = new GraphUIDFactory();

    Scanner scanner = null;

    paths = new Vector<Path>(1);
    origins = new Vector<Source>(1);
    destinations = new Vector<Destination>(1);
    System.out.println("Opening " + name);
    try {
      scanner = new Scanner(new File(name));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // The first line is the number of intersections
    int nb_nodes = scanner.nextInt();
    nodes = new Vector<Node>(nb_nodes);
    for (int i = 0; i < nb_nodes; i++) {
      nodes.add(null);
    }
    Node tmp_node;
    for (int i = 0; i < nb_nodes; i++) {
      tmp_node = new Node(id_factory);
      nodes.set(tmp_node.unique_id, tmp_node);
    }
    scanner.nextLine();

    // The second line is the number of links
    int number_links = scanner.nextInt();
    links = new Vector<Link>(number_links);
    for (int i = 0; i < number_links; i++) {
      links.add(null);
    }

    double length, v, w, f_max, jam_density;
    Link tmp;
    for (int id = 0; id < number_links; id++) {
      scanner.nextLine();
      length = scanner.nextDouble();
      v = scanner.nextDouble();
      w = scanner.nextDouble();
      f_max = scanner.nextDouble();
      jam_density = scanner.nextInt();
      tmp = new Link(length, v, w, f_max, jam_density, id_factory);
      links.set(tmp.unique_id, tmp);

      nodes.get(scanner.nextInt()).addOutgoingLink(tmp);
      nodes.get(scanner.nextInt()).addIncomingLink(tmp);
    }
    // Sources
    scanner.nextLine();
    int nb_sources = scanner.nextInt();
    scanner.nextLine();
    for (int i = 0; i < nb_sources; i++) {
      origins.add(new Source(nodes.get(scanner.nextInt()).unique_id, "Origin"));
    }

    // Destinations
    scanner.nextLine();
    int nb_dest = scanner.nextInt();
    scanner.nextLine();
    for (int i = 0; i < nb_dest; i++) {
      destinations.add(new Destination(nodes.get(scanner.nextInt()).unique_id,
          "Destination"));
    }
    scanner.close();
  }

  public void addSingleBufferSource(Node s) {
    origins.add(new Source(s.unique_id, "SingleBuffer"));
  }

  public void addMultipleBufferSource(Node s) {
    origins.add(new Source(s.unique_id, "MultipleBuffer"));
  }

  public void addSingleBufferDestination(Node d) {
    destinations.add(new Destination(d.unique_id, "SingleJunction"));
  }

  public void addMultipleBufferDestination(Node d) {
    destinations.add(new Destination(d.unique_id, "MultipleBuffer"));
  }

  public void addPath(Path p) {
    paths.add(p);
  }

  public void addPath(ArrayList<Integer> p) {
    paths.add(new Path(p));
  }
}

package generalNetwork.graph;

import generalNetwork.graph.json.JsonFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 * @brief Allows to edit the creation of a network
 * @details
 *          To switch between modes type:
 *          - 'n' to be in the NODE mode: when clicking you create a node
 *          - 'l' to be in the LINK mode: you can create a link between node A
 *          to B by first clicking on A and then B. Clicking "on" take the
 *          closest node
 *          - 'o' to be in the ORIGIN mode: click on a node to transform it into
 *          an origin. They will be displayed in red.
 *          - 'd' to be in the DESTINATION mode: click on a node to transform it
 *          into a destination. They will be displayed in blue.
 */
public class EditorPanel extends JPanel implements MouseListener, KeyListener {

  public enum TypeClicked {
    NODE,
    LINK,
    ORIGIN,
    DESTINATION
  }

  private static final long serialVersionUID = 1L;
  private MutableGraph graph;
  private TypeClicked type;
  private Node clicked_node_1, clicked_node_2;
  private int circle_radius = 10;

  public EditorPanel() {
    super();
    graph = new MutableGraph();
    type = TypeClicked.NODE;
    System.out.println("Editor created");
    addMouseListener(this);
    setFocusable(true);
    addKeyListener(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    System.out.println("Click on (" + x + "," + y + ")");
    switch (type) {
    case NODE:
      graph.addNode(x, y);
      repaint();
      break;
    case LINK:
      if (clicked_node_1 == null)
        clicked_node_1 = graph.searchNode(x, y);
      else {
        System.out.println("Linking two nodes");
        clicked_node_2 = graph.searchNode(x, y);
        graph.addLink(clicked_node_1, clicked_node_2);
        clicked_node_1 = null;
        clicked_node_2 = null;
        repaint();
      }
      break;
    case ORIGIN:
      Node o = graph.searchNode(x, y);
      if (o != null)
        graph.addSingleBufferSource(o);
      repaint();
      break;
    case DESTINATION:
      Node s = graph.searchNode(x, y);
      if (s != null)
        graph.addSingleBufferDestination(s);
      repaint();
      break;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D graphics2D = (Graphics2D) g;
    graphics2D.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    /* First we print the links */
    int Mx, My, Bx, By;
    Iterator<Link> links_it = graph.links.iterator();
    Link link;
    while (links_it.hasNext()) {
      link = links_it.next();
      Mx = (link.from.x + link.to.x) / 2;
      My = (link.from.y + link.to.y) / 2;
      Bx = link.from.x + (-link.from.x + link.to.x) / (3);
      By = link.from.y + (-link.from.y + link.to.y) / (3);
      g.drawLine(link.from.x, link.from.y, link.to.x, link.to.y);

      drawArrowHead(graphics2D, Mx, My, Bx, By, Color.red);
    }

    /* And then the nodes */
    Iterator<Node> nodes_it = graph.nodes.iterator();
    Node node;
    while (nodes_it.hasNext()) {
      node = nodes_it.next();

      drawNode(graphics2D, node, Color.black);
    }

    Iterator<Source> sources_it = graph.origins.iterator();
    Source source;
    while (sources_it.hasNext()) {
      source = sources_it.next();
      drawNode(graphics2D, graph.nodes.get(source.id), Color.red);
    }

    Iterator<GraphDestination> destination_it = graph.destinations.iterator();
    GraphDestination destination;
    while (destination_it.hasNext()) {

      destination = destination_it.next();
      System.out.println("Destination" + destination.id);
      drawNode(graphics2D, graph.nodes.get(destination.id), Color.blue);
    }
  }

  private void drawNode(Graphics2D g2, Node node, Color color) {
    Paint p = g2.getPaint();
    g2.setPaint(color);
    g2.fillOval(node.x - circle_radius / 2, node.y - circle_radius / 2,
        circle_radius, circle_radius);
    g2.setPaint(p);
  }

  private void drawArrowHead(Graphics2D g2, int head_x, int head_y, int tail_x,
      int tail_y, Color color)
  {
    double phi = Math.toRadians(40);
    int barb = 18;
    Paint p = g2.getPaint();
    g2.setPaint(color);
    double dy = head_y - tail_y;
    double dx = head_x - tail_x;
    double theta = Math.atan2(dy, dx);
    double x, y, rho = theta + phi;
    for (int j = 0; j < 2; j++)
    {
      x = head_x - barb * Math.cos(rho);
      y = head_y - barb * Math.sin(rho);
      g2.draw(new Line2D.Double(head_x, head_y, x, y));
      rho = theta - phi;
    }

    g2.setPaint(p);
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
    switch (e.getKeyChar()) {
    case 'n':
      System.out.println("Node mode selected");
      type = TypeClicked.NODE;
      clicked_node_1 = null;
      clicked_node_2 = null;
      break;
    case 'l':
      System.out.println("Link mode selected");
      type = TypeClicked.LINK;
      clicked_node_1 = null;
      clicked_node_2 = null;
      break;
    case 'o':
      System.out.println("Origin mode selected");
      type = TypeClicked.ORIGIN;
      clicked_node_1 = null;
      clicked_node_2 = null;
      break;
    case 'd':
      System.out.println("Destination mode selected");
      type = TypeClicked.DESTINATION;
      clicked_node_1 = null;
      clicked_node_2 = null;
      break;
    case 's':
      System.out.println("Saving in temporary.json");
      Graph fixed_graph = new Graph(graph);
      JsonFactory json = new JsonFactory(true);
      json.toFile(fixed_graph, "temporary.json");
    }
  }
}
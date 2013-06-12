package generalNetwork.graph;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.JPanel;

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

  private void createOrigin(int x, int y) {
  }

  private void createDestination(int x, int y) {
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
      createOrigin(x, y);
      break;
    case DESTINATION:
      createDestination(x, y);
      break;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Iterator<Node> nodes_it = graph.nodes.iterator();
    Iterator<Link> links_it = graph.links.iterator();
    Node node;
    Link link;
    while (nodes_it.hasNext()) {
      node = nodes_it.next();
      g.fillOval(node.x, node.y, circle_radius, circle_radius);
    }

    while (links_it.hasNext()) {
      link = links_it.next();
      g.drawLine(link.from.x, link.from.y, link.to.x, link.to.y);
    }
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
    }
  }
}
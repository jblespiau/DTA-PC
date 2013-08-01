package generalNetwork.graph;

import generalLWRNetwork.Cell;
import generalLWRNetwork.DiscretizedGraph;
import generalLWRNetwork.LWR_network;
import generalLWRNetwork.RoadChunk;
import generalNetwork.state.Profile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import dta_solver.Simulator;

/**
 * @brief Allows to display a state on a network
 */
public class StatePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private DiscretizedGraph graph;
  private LWR_network lwr_network;
  private Profile p;
  private int circle_radius = 4;

  public StatePanel(Simulator s) {
    super();
    graph = s.discretized_graph;
    this.lwr_network = s.lwr_network;
  }

  public void setSimulation(Simulator s) {
    graph = s.discretized_graph;
    this.lwr_network = s.lwr_network;
  }

  public void displayProfile(Profile p) {
    this.p = p;
    repaint();
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
    Link[] links = graph.graph_links;
    Link link;
    Cell begin, end, tmp;
    int nb_cells;
    double dx, dy;
    /* Position of the beginning/end of a cell */
    int tail_x, tail_y, head_x, head_y;

    for (int i = 0; i < links.length; i++) {
      link = links[i];
      begin = graph.firstCellofLink(i);
      end = graph.lastCellofLink(i);
      /* First we need to count the number of cells */
      tmp = begin;
      nb_cells = 1;
      while (tmp.getUniqueId() != end.getUniqueId()) {
        tmp = tmp.getNext().getNext()[0];
        nb_cells++;
      }
      /* Then we print the density for them */
      dx = (link.to.x - link.from.x) / (double) nb_cells;
      dy = (link.to.y - link.from.y) / (double) nb_cells;

      tail_x = link.from.x;
      tail_y = link.from.y;

      tmp = graph.firstCellofLink(i);
      for (int c = 0; c < nb_cells; c++) {
        head_x = (int) Math.round(((double) link.from.x) + c * dx);
        head_y = (int) Math.round(((double) link.from.y) + c * dy);

        if (c == nb_cells - 1) {
          head_x = link.to.x;
          head_y = link.to.y;
        }
        double lambda = (p.getCell(tmp).total_density / tmp.getJamDensity(0));
        double magic_coef = 0.4;
        float green = (float) 0.3;
        float red = 1;
        float color = green;
        RoadChunk rc = (RoadChunk) tmp;
        if (rc.isCongested(p.getCell(tmp).total_density, 0)) {
          color = red;
          lambda = (p.getCell(tmp).total_density / tmp.getJamDensity(0));
        }
        graphics2D.setPaint(
            Color.getHSBColor((float) color,
                (float) (lambda + (1 - lambda) * magic_coef),
                (float) 1));

        graphics2D.setStroke(new BasicStroke(3));
        graphics2D.drawLine(head_x, head_y, tail_x, tail_y);
        tail_x = head_x;
        tail_y = head_y;

        tmp = tmp.getNext().getNext()[0];
      }

      Mx = (link.from.x + link.to.x) / 2;
      My = (link.from.y + link.to.y) / 2;
      Bx = link.from.x + (-link.from.x + link.to.x) / (3);
      By = link.from.y + (-link.from.y + link.to.y) / (3);

      graphics2D.setStroke(new BasicStroke(1));
      drawArrowHead(graphics2D, Mx, My, Bx, By, Color.black);
    }

    /* And then the nodes */
    Node[] nodes = graph.graph_nodes;
    Node node;
    for (int i = 0; i < nodes.length; i++) {
      node = nodes[i];

      drawNode(graphics2D, node, Color.black);
    }

    Source[] sources = graph.graph_origins;
    Source source;
    for (int i = 0; i < sources.length; i++) {
      source = sources[i];
      drawNode(graphics2D, nodes[source.id], Color.red);
    }

    GraphDestination[] destinations = graph.graph_destinations;
    GraphDestination destination;
    for (int i = 0; i < destinations.length; i++) {

      destination = destinations[i];
      drawNode(graphics2D, nodes[destination.id], Color.blue);
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
    int barb = 5;
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

}
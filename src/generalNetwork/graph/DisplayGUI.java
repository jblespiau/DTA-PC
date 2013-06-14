package generalNetwork.graph;

import generalNetwork.state.Profile;

import java.awt.Color;

import javax.swing.JFrame;

import dta_solver.Simulator;

public class DisplayGUI extends JFrame {

  private static final long serialVersionUID = 1L;
  private StatePanel pan;

  public enum GUI_Type {
    EDITOR,
    DISPLAY
  }

  public DisplayGUI(Simulator s) {
    this.setTitle("Display");
    this.setSize(300, 300);
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    pan = new StatePanel(s);
    pan.setBackground(Color.WHITE);
    this.add(pan);
    this.setContentPane(pan);
    this.setVisible(true);
  }

  public void displayState(Profile p) {
    pan.displayProfile(p);
  }
}
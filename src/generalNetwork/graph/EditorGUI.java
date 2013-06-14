package generalNetwork.graph;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class EditorGUI extends JFrame {

  private static final long serialVersionUID = 1L;

  public EditorGUI() {
    this.setTitle("Editor");
    this.setSize(300, 300);
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel pan = new EditorPanel();
    pan.setBackground(Color.WHITE);
    this.add(pan);
    this.setContentPane(pan);
    this.setVisible(true);
  }
}
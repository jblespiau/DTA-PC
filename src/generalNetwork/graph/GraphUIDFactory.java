package generalNetwork.graph;

/**
 * @brief Unique ID factory for nodes and links
 * 
 */
public class GraphUIDFactory {

  private int id_link = -1, id_node = -1, id_path = -1;

  public GraphUIDFactory() {
  }

  public GraphUIDFactory(int id_link, int id_node, int id_path) {
    this.id_link = id_link;
    this.id_node = id_node;
    this.id_path = id_path;
  }

  public int getId_node() {
    id_node++;
    return id_node;
  }

  public int getId_link() {
    id_link++;
    return id_link;
  }

  public int getId_Path() {
    id_path++;
    return id_path;
  }

  public void resetNode_id() {
    id_node = -1;
  }

  public void resetLink_id() {
    id_link = -1;
  }

  public void resetPath_id() {
    id_path = -1;
  }

  public int IdNode() {
    return id_node;
  }

  public int IdLink() {
    return id_link;
  }

  public int IdPath() {
    return id_path;
  }
}
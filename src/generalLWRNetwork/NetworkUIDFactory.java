package generalLWRNetwork;

/**
 * @brief Unique ID factory for cells and junctions
 */
public final class NetworkUIDFactory {

  static int id_cell = -1, id_junctions = -1;

  private NetworkUIDFactory() {
  }

  static int getId_cell() {
    id_cell++;
    return id_cell;
  }

  static int getId_junctions() {
    id_junctions++;
    return id_junctions;
  }

  public static void resetCell_id() {
    id_cell = -1;
  }

  public static void resetJunction_id() {
    id_junctions = -1;
  }

  static int IdCell() {
    return id_cell;
  }

  static int IdJunction() {
    return id_junctions;
  }
}
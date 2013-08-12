package generalNetwork.state;

import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;
import generalLWRNetwork.LWR_network;

/**
 * @class Profile
 * @brief Contains all the information of the cells and junctions for a given
 *        time step
 */
public class Profile {

  /** Information for all cells */
  private CellInfo[] profile;
  /* Used to keep the aggregate split ratios */
  /** Information for all junctions */
  public JunctionInfo[] junction_info;

  /**
   * @brief Creates a new profile describing the state for a network composed of
   *        the given elements
   * @param nb_cells
   *          Number of cells in the network
   * @param nb_junctions
   *          Number of junctions in the network
   */
  public Profile(int nb_cells, int nb_junctions) {
    super();
    profile = new CellInfo[nb_cells];
    junction_info = new JunctionInfo[nb_junctions];
  }

  /**
   * @brief Creates a new profile for a given network
   * @param network
   */
  public Profile(LWR_network network) {
    this(network.getCells().length, network.getJunctions().length);
  }

  public CellInfo getCell(int i) {
    return profile[i];
  }

  public CellInfo getCell(Cell c) {
    return profile[c.getUniqueId()];
  }

  public JunctionInfo getJunction(int i) {
    return junction_info[i];
  }

  public JunctionInfo getJunction(Junction j) {
    return junction_info[j.getUniqueId()];
  }

  public void putCell(int cell_id, CellInfo info) {
    profile[cell_id] = info;
  }

  public void putJunction(int j_id, JunctionInfo info) {
    junction_info[j_id] = info;
  }

  public int CellInfoSize() {
    return profile.length;
  }

  public int JunctionsInfoSize() {
    return junction_info.length;
  }

  CellInfo[] getCellInfos() {
    return profile;
  }

  public void print() {
    for (int i = 0; i < profile.length; i++) {
      System.out.print(i + "->");
      profile[i].print();
      System.out.println();
    }
  }

  public boolean equals(Object obj, double epsilon) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Profile other = (Profile) obj;

    if (other.profile.length != profile.length)
      return false;

    for (int i = 0; i < profile.length; i++) {
      if (!other.profile[i].equals(profile[i], epsilon))
        return false;
    }

    return true;
  }

  /**
   * @brief Copy the profile with only the densities information
   * @return
   */
  public Profile copy() {
    int cells_length = this.profile.length;
    int junctions_length = this.junction_info.length;
    Profile result = new Profile(cells_length,
        junctions_length);
    for (int i = 0; i < cells_length; i++)
      result.profile[i] = this.profile[i].copy();

    return result;
  }
}

package generalNetwork.state;

import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;

public class Profile {

  private CellInfo[] profile;
  /* Used to keep the aggregate split ratios */
  private JunctionInfo[] junction_info;


  public Profile(int nb_cells, int nb_junctions) {
    super();
    profile = new CellInfo[nb_cells];
    junction_info = new JunctionInfo[nb_junctions];
  }

  public CellInfo get(int i) {
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
}

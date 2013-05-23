package generalNetwork.state;

import generalLWRNetwork.Cell;

public class Profile {

  private CellInfo[] profile;

  public Profile(int nb_cells) {
    super();
    profile = new CellInfo[nb_cells];
  }

  public CellInfo get(int i) {
    return profile[i];
  }

  public CellInfo get(Cell c) {
    return profile[c.getUniqueId()];
  }

  public void put(int cell_id, CellInfo info) {
    profile[cell_id] = info;
  }

  public int size() {
    return profile.length;
  }

  CellInfo[] getProfile() {
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

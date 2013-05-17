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
}

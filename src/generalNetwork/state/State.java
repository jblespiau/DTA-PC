package generalNetwork.state;

import generalNetwork.state.Profile;

public class State {

    private Profile[] state;

    public State(int nb_steps) {
        super();
        state = new Profile[nb_steps];
    }

    public void put(int k, Profile p) {
        state[k] = p;
    }

    public Profile get(int k) {
        return state[k];
    }

    public int size() {
        return state.length;
    }
}

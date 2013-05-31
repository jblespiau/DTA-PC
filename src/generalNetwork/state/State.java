package generalNetwork.state;

import org.wsj.SystemState;

import generalNetwork.state.Profile;

public class State implements SystemState {

  public Profile[] profiles;

  public State(Profile[] s) {
    profiles = s;
  }

  public void put(int k, Profile p) {
    profiles[k] = p;
  }

  public Profile get(int k) {
    return profiles[k];
  }

  public int size() {
    return profiles.length;
  }

  @Override
  public Object getState() {
    return this;
  }
}
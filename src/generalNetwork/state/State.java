package generalNetwork.state;

import org.wsj.SystemState;

import dta_solver.adjointMethod.JavaSystemState;
import generalNetwork.state.Profile;

public class State implements JavaSystemState, SystemState {

  public Profile[] profiles;
  /* sum[orig][k] saves the sum at the orig for time step k */
  public double[][] sum_of_split_ratios;

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
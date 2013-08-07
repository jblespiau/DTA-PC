package generalNetwork.state;

import dta_solver.adjointMethod.JavaSystemState;
import generalNetwork.state.Profile;

/**
 * @package generalNetwork.state
 * @brief Elements related to the data in a state profile (i.e. state of the network for all time steps)
 */

/**
 * @class State
 * @brief Contains the state of all cells and junctions for a given simulation
 */
public class State implements JavaSystemState {

  /** Profile of the network for every time step */
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
}
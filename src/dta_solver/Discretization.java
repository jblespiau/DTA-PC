package dta_solver;

/**
 * @class Environment
 * @brief Used now to save global variables.
 * 
 */
public class Discretization {

	private double delta_t;
	private int nb_steps = 1;

	public Discretization(double delta_t, int nb_steps) {
		this.delta_t = delta_t;
		this.nb_steps = nb_steps;
	}
	
	public double getDelta_t() {
		return delta_t;
	}

	void setDelta_t(double delta_t) {
		this.delta_t = delta_t;
	}

	public int getNb_steps() {
		return nb_steps;
	}

	void setNb_steps(int nb_steps) {
		this.nb_steps = nb_steps;
	}
}

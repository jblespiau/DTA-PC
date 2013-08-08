package dataStructures;

/**
 * @package dataStructures 
 * @brief Data structures and global constants
 */
/**
 * @class Preprocessor
 * @brief Contains global constants used to control the behavior of the system
 */
public final class Preprocessor {

  /** Whether the rounding up of negative densities values should be printed */
  static final public boolean ZERO_ROUND_NOTIFICATION = false;

  /**
   * When checking that the network is emptied at the end of the simulation, it
   * will print warnings if the condition is not strictly satisfied.
   * Even when set to true, the verification will return true if all densities
   * are smaller than 10E-10
   */
  static final public boolean WARNING_STRICLY_EMPTY = false;
}
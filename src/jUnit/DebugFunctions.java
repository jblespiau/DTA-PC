package jUnit;

import dataStructures.Numerical;

public final class DebugFunctions {

  public static boolean close(double[] t1, double[] t2) {
    return close(t1, t2, 10E-6);
  }

  public static boolean close(double[] t1, double[] t2, double error) {
    assert (t1.length == t2.length);
    boolean result = true;
    int nb_differences = 0;
    for (int i = 0; i < t1.length; i++) {
      if (!Numerical.close(t1[i], t2[i], error)) {
        result = false;
        nb_differences++;
        System.out.println("Difference in (" + i + ") from t1 (" + t1[i]
            + ") and t2 (" + t2[i] + ")");
      }
    }
    if (nb_differences != 0)
      System.out.println("Error: total number of differences: "
          + nb_differences);
    return result;
  }

  public static boolean compareTable(double[][] t1, double[][] t2) {
    return compareTable(t1, t2, 10E-6);
  }

  /**
   * @brief Return true if t1 and t2 are identical
   */
  public static boolean compareTable(double[][] t1, double[][] t2, double error) {

    assert (t1.length == t2.length) : "Comparison between tables of different sizes: "
        + t1.length + " and " + t2.length;
    assert (t1[0].length == t2[0].length) : "Comparison between tables of different sizes"
        + t1[0].length + " and " + t2[0].length;
    boolean result = true;
    int nb_differences = 0;
    for (int i = 0; i < t1.length; i++) {
      for (int j = 0; j < t1[0].length; j++) {
        if (!Numerical.equals(t1[i][j], t2[i][j], error)) {
          result = false;
          nb_differences++;
          System.out.println("Difference in (" + i + ", " + j
              + ") from t1 (" + t1[i][j] + ") and t2 (" + t2[i][j] + ")");
        }
      }
    }
    if (nb_differences != 0)
      System.out.println("Error: total number of differences: "
          + nb_differences);
    return result;
  }

  public static boolean compareTable(double[] t1, double[] t2) {
    return compareTable(t1, t2, 10E-6);
  }

  public static boolean compareTable(double[] t1, double[] t2, double error) {
    assert (t1.length == t2.length) : "Tables of different size " + t1.length
        + " and " + t2.length;
    boolean result = true;
    int nb_differences = 0;
    for (int i = 0; i < t1.length; i++) {
      if (!Numerical.equals(t1[i], t2[i], error)) {
        result = false;
        nb_differences++;
        System.out.println("Difference in (" + i + ") from t1 (" + t1[i]
            + ") and t2 (" + t2[i] + ")");
      }
    }
    if (nb_differences != 0)
      System.out.println("Error, the 2 tables have " + nb_differences
          + " differences");
    return result;
  }
}

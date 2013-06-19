package dataStructures;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Numerical {

  private static double epsilon = 0.0001;

  static public boolean validNumber(double a) {
    return !(Double.isInfinite(a) || Double.isNaN(a));
  }

  static public boolean isLowerTrangular(double[][] matrix) {
    for (int i = 0; i < matrix.length; i++)
      for (int j = i + 1; j < matrix.length; j++)
        if (matrix[i][j] != 0)
          return false;

    return true;
  }

  static public boolean NonSingularLowerTriangular(double[][] matrix) {
    if (!isLowerTrangular(matrix))
      return false;
    if (matrix.length != matrix[0].length)
      return false;
    for (int i = 0; i < matrix.length; i++)
      if (matrix[i][i] == 0)
        return false;

    return true;
  }

  static public boolean equals(double a, double b, double e) {
    return Math.abs(a - b) < e;
  }

  /**
   * @brief Compare the two Double with a precision of epsilon.
   *        If a value is null, it is considered to be zero
   */
  static public boolean equals(Double a, Double b, double e) {
    if (a == null)
      a = 0.0;
    if (b == null)
      b = 0.0;

    return Math.abs(a - b) < e;
  }

  static public boolean equals(Entry<Integer, Double> e1,
      Entry<Integer, Double> e2,
      double epsilon)
  {
    return (e1.getKey() == null ?
        e2.getKey() == null : e1.getKey().equals(e2.getKey())) &&
        (e1.getValue() == null ?
            e2.getValue() == null : equals(e1.getValue(), e2.getValue(),
                epsilon));
  }

  static public boolean equals(LinkedHashMap<Integer, Double> m1,
      LinkedHashMap<Integer, Double> m2, double epsilon) {

    /*
     * We check that both map contains the other.
     * Be aware that a null Double or a zero Double is considered to be the same
     */

    Iterator<Entry<Integer, Double>> it = m1.entrySet().iterator();
    Entry<Integer, Double> entry;
    while (it.hasNext()) {
      entry = it.next();
      if (!equals(entry.getValue(), m2.get(entry.getKey()), epsilon)) {
        return false;
      }
    }

    it = m2.entrySet().iterator();
    while (it.hasNext()) {
      entry = it.next();
      if (!equals(entry.getValue(), m1.get(entry.getKey()), epsilon)) {
        return false;
      }
    }

    return true;
  }

  /**
   * @return an optimistic (a >= b) (if a is nearly greater, it returns true)
   */
  static boolean greaterThan(double a, double b) {
    return a + epsilon > b;
  }

  static double getEpsilon() {
    return epsilon;
  }
}
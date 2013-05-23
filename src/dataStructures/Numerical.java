package dataStructures;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Numerical {

  private static double epsilon = 0.0001;

  static public boolean equals(double a, double b, double e) {
    return Math.abs(a - b) < e;
  }

  static public boolean equals(Double a, Double b, double e) {
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

    // We check that both map have the same keyset
    if (!m1.keySet().equals(m2.keySet()))
      return false;
    Iterator<Entry<Integer, Double>> it = m1.entrySet().iterator();
    Entry<Integer, Double> entry;
    while (it.hasNext()) {
      entry = it.next();
      if (!equals(entry.getValue(), m2.get(entry.getKey()), epsilon)) {
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
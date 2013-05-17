package dataStructure;

import java.util.LinkedHashMap;

/**
 * @brief Optimized HashMap
 * @details This HashMap maps non negative double and is optimized for mapping
 *          values containing an important share of zero. If the entry does not
 *          exist it means that the value is zero.
 *          This implementation catches the null return value and return 0
 *          instead. It ensures we never put zero value in the underlying
 *          HashMap
 *          Moreover, we can directly modify a searched value with
 *          mutable wrapper of double
 * 
 */
public class HollowHashMap extends LinkedHashMap<Integer, MutableDouble> {

  private static final long serialVersionUID = 1L;

  /**
   * @brief This put function ensures to never add zero values
   * @return Always null
   */
  public Double put(Integer i, double b) {

    if (b == 0)
      super.remove(i);
    else
      super.put(i, new MutableDouble(b));

    return null;

  }

  /**
   * @brief This function catch null result and turns them into zero value
   * @return 0 if the key is not found and the value otherwise
   */
  public MutableDouble get(Integer i) {
    MutableDouble result = super.get(i);
    if (result == null)
      return new MutableDouble(0.0);
    else
      return result;
  }
}
package dataStructure;

public class MutableDouble {

  public double d;

  public MutableDouble(double value) {
    d = value;
  }

  public MutableDouble(Double value) {
    d = value.doubleValue();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(d);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MutableDouble other = (MutableDouble) obj;
    if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d))
      return false;
    return true;
  }

}

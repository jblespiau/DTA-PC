package generalNetwork.state.splitRatios;

public class PairBufferCommodity {

  public int buffer_id, commodity;

  public PairBufferCommodity(int x, int y) {
    this.buffer_id = x;
    this.commodity = y;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + buffer_id;
    result = prime * result + commodity;
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
    PairBufferCommodity other = (PairBufferCommodity) obj;
    if (buffer_id != other.buffer_id)
      return false;
    if (commodity != other.commodity)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PairBufferCommodity [buffer_id=" + buffer_id + ", c="
        + commodity + "]";
  }
}

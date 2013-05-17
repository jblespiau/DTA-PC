package generalNetwork.state.splitRatios;

public class Triplet {

  public int incoming, outgoing, commodity;

  public Triplet(int incoming, int outgoing, int commodity) {
    this.incoming = incoming;
    this.outgoing = outgoing;
    this.commodity = commodity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + commodity;
    result = prime * result + incoming;
    result = prime * result + outgoing;
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
    Triplet other = (Triplet) obj;
    if (commodity != other.commodity)
      return false;
    if (incoming != other.incoming)
      return false;
    if (outgoing != other.outgoing)
      return false;
    return true;
  }
}

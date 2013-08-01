package generalLWRNetwork;

/**
 * @class Sink
 * @brief Represent a bottleneck sink (supply is bounded)
 */
class SinkBottleneck extends Sink {

  private double bottleneck_capacity;

  public SinkBottleneck(double bottleneck_capacity) {
    super();
    this.bottleneck_capacity = bottleneck_capacity;
  }

  @Override
  public double getSupply(double density, int time_step) {
    return bottleneck_capacity;
  }

  @Override
  public String toString() {
    return "[(" + getUniqueId() + ")Bottleneck: " + bottleneck_capacity
        + "]";
  }
}
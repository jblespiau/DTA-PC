public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    boolean debug = false;
    String network_file = "graphs/parallelPath.json";
    String data_file = "graphs/parallelPathData.json";

    Simulator simulator = new Simulator(network_file, data_file,debug);
    simulator.run(debug);
  }
}
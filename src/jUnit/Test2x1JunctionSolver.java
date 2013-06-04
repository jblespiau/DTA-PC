package jUnit;

import static org.junit.Assert.*;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;
import generalLWRNetwork.NetworkUIDFactory;
import generalLWRNetwork.RoadChunk;
import generalNetwork.state.CellInfo;
import generalNetwork.state.JunctionInfo;
import generalNetwork.state.Profile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test2x1JunctionSolver {

  static int number_tests = 2;

  /* 2x1 junction */
  static private Junction junction2x1;
  static private Cell[] cells;
  static private double delta_t;

  /**
   * @brief Build a 2x1 junction
   */
  private static void build(double delta_t) {

    Test2x1JunctionSolver.delta_t = delta_t;
    NetworkUIDFactory.resetCell_id();
    NetworkUIDFactory.resetJunction_id();

    // Constructing the 2x1 junction
    RoadChunk rc1 = new RoadChunk(3.0, 3.0, 3.0, delta_t);
    RoadChunk rc2 = new RoadChunk(2.0, 2.0, 2.0, delta_t);

    RoadChunk rc3 = new RoadChunk(2.5, 2.5, 2.0, delta_t);

    junction2x1 = new Junction(new Cell[] { rc1, rc2 }, new Cell[] { rc3 });
    HashMap<Integer, Double> priorities = new HashMap<Integer, Double>(2);
    priorities.put(rc1.getUniqueId(), 0.7);
    priorities.put(rc2.getUniqueId(), 0.3);
    junction2x1.setPriorities(priorities);

    cells = new Cell[3];
    cells[0] = rc1;
    cells[1] = rc2;
    cells[2] = rc3;
  }

  private static void computeDemandSupply(Profile p) {
    double density, demand, supply;
    for (int cell_id = 0; cell_id < cells.length; cell_id++) {
      density = p.getCell(cell_id).total_density;

      /*
       * The demand and the supply depend on the network and the density
       */
      demand = cells[cell_id].getDemand(density, delta_t);
      supply = cells[cell_id].getSupply(density);
      assert demand >= 0 : "Demand should be positive";
      assert supply >= 0 : "Supply should be positive";

      p.getCell(cell_id).demand = demand;
      p.getCell(cell_id).supply = supply;

      // We clear the old flows
      p.getCell(cell_id).clearFlow();
    }
  }

  public static void createProfile() {
    createProfile(0.7);
    createProfile(1.0);
    createProfile(2.3);
  }

  private static void createProfile(double delta_t) {
    build(delta_t);

    Gson gson = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting().create();

    Writer writer = null;
    Profile p = null;
    for (int i = 0; i < number_tests; i++) {
      try {
        writer = new FileWriter(
            "JUnitTests/solutions/2x1junction-initial_profile" + i + "T"
                + delta_t);
      } catch (Exception e) {
        e.printStackTrace();
      }

      p = new Profile(cells.length, 1);
      LinkedHashMap<Integer, Double> densities_1 = new LinkedHashMap<Integer, Double>();
      LinkedHashMap<Integer, Double> densities_2 = new LinkedHashMap<Integer, Double>();
      if (i == 0) {
        densities_1.put(1, 0.5);
        densities_2.put(1, 0.5);
      } else if (i == 1) {
        densities_1.put(1, 1.0);
        densities_1.put(2, 0.8);
        densities_2.put(1, 2.0);
        densities_2.put(2, 0.7);
      }
      p.putCell(0, new CellInfo(densities_1));
      p.putCell(1, new CellInfo(densities_2));
      p.putCell(2, new CellInfo());

      computeDemandSupply(p);

      gson.toJson(p, Profile.class, writer);
      try {
        if (writer != null)
          writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @brief Save simulations for different delta_t
   */
  public static void register2x1Junction() {
    register2x1Junction(0.7);
    register2x1Junction(1.0);
    register2x1Junction(2.3);
  }

  /**
   * @brief Save the simulation for the parallelPath network into file
   */
  private static void register2x1Junction(double delta_t) {
    build(delta_t);

    Gson gson = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting().create();

    Profile initial;
    for (int i = 0; i < number_tests; i++) {
      Reader reader = null;
      Writer writer = null;
      try {
        reader = new FileReader(
            "JUnitTests/solutions/2x1junction-initial_profile" + i + "T"
                + delta_t);
        writer = new FileWriter(
            "JUnitTests/solutions/2x1junction-result_profile" + i + "T"
                + delta_t);
      } catch (IOException e) {
        e.printStackTrace();
      }

      initial = gson.fromJson(reader, Profile.class);
      junction2x1.solveJunction(initial, 0, null, cells);

      gson.toJson(initial, Profile.class, writer);

      try {
        if (reader != null)
          reader.close();
        if (writer != null)
          writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @brief Imports initial profile and split ratios, solve the junction and
   *        check the result with a saved solution
   */
  @Test
  public void test2x1Junction() {
    run(0.7);
    run(1.0);
    run(2.3);
  }

  private void run(double delta_t) {
    build(delta_t);

    Gson gson = new Gson();
    Profile initial;
    Profile result;
    for (int i = 0; i < number_tests; i++) {
      Reader reader1 = null, reader2 = null;
      try {
        reader1 = new FileReader(
            "JUnitTests/solutions/2x1junction-initial_profile" + i + "T"
                + delta_t);
        reader2 = new FileReader(
            "JUnitTests/solutions/2x1junction-result_profile" + i + "T"
                + delta_t);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

      /* We only check the state of the cells and not the aggregate split ratios */
      initial = gson.fromJson(reader1, Profile.class);
      initial.junction_info = new JunctionInfo[1];
      result = gson.fromJson(reader2, Profile.class);

      junction2x1.solveJunction(initial, 0, null, cells);
      initial.junction_info = null;

      System.out.println(gson.toJson(initial, Profile.class));
      System.out.println(gson.toJson(result, Profile.class));
      assertTrue("2x1junction-result_profile" + i + "T"
          + delta_t + "failed",
          result.equals(initial, 1e-6));

      try {
        if (reader1 != null)
          reader1.close();
        if (reader2 != null)
          reader2.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
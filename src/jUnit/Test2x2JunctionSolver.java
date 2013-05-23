package jUnit;

import static org.junit.Assert.*;
import generalLWRNetwork.Cell;
import generalLWRNetwork.Junction;
import generalLWRNetwork.RoadChunk;
import generalNetwork.state.Profile;
import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;

import org.junit.Test;

import com.google.gson.Gson;

import dta_solver.Discretization;

public class Test2x2JunctionSolver {

  static int number_tests = 5;

  static private Discretization discretization;
  /* 2x1 junction */
  static private Junction junction2x1;
  static private Cell[] cells;
  static private Profile initial_profile;
  static private Profile result_profile;

  public static void build(double delta_t) {
    discretization = new Discretization(delta_t, 1);

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

  public static void register2RandomProfile() {
    // Creates random profile with split ratios
    /*
    writer1 = new FileWriter(
        "JUnitTests/solutions/2x1junction-initial_profile" + i);
    writer2 = new FileWriter(
        "JUnitTests/solutions/2x1junction-result_profile" + i); */
  }

  /**
   * @brief Save the simulation for the parallelPath network into file
   */
  public static void register2x1Junction() {

    Gson gson = new Gson();

    Profile initial;
    JunctionSplitRatios junction_sr;
    for (int i = 0; i < number_tests; i++) {
      Reader reader1 = null, reader2 = null;
      Writer writer = null;
      try {
        reader1 = new FileReader(
            "JUnitTests/solutions/2x1junction-initial_profile" + i);
        reader2 = new FileReader(
            "JUnitTests/solutions/2x1junction-split_ratios" + i);
        writer = new FileWriter(
            "JUnitTests/solutions/2x1junction-result_profile" + i);
      } catch (IOException e) {
        e.printStackTrace();
      }

      initial = gson.fromJson(reader1, Profile.class);
      junction_sr = gson.fromJson(reader2, JunctionSplitRatios.class);
      junction2x1.solveJunction(initial, 0, junction_sr, cells);

      gson.toJson(initial, Profile.class, writer);

      try {
        if (reader1 != null)
          reader1.close();
        if (reader2 != null)
          reader2.close();
        if (writer != null)
          writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
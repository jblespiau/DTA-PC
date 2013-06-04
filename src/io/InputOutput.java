package io;

import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class InputOutput {

  static public void printTable(double[][] table) {

    for (int i = 0; i < table.length; i++) {
      for (int j = 0; j < table[0].length; j++)
        System.out.printf("%9.4f ", table[i][j]);
      System.out.println();
    }
  }

  static public Writer Writer(String file_name) {

    Writer writer = null;
    try {
      writer = new FileWriter(file_name);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return writer;
  }

  static public Reader Reader(String file_name) {

    Reader reader = null;
    try {
      reader = new FileReader(file_name);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return reader;
  }

  static public void close(Closeable stream) {
    try {
      if (stream != null)
        stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
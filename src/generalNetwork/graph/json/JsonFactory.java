package generalNetwork.graph.json;

import generalNetwork.graph.Graph;
import generalNetwork.graph.Node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonFactory {

  public Gson gson;

  public JsonFactory(boolean debug_mode) {
    GsonBuilder builder = new GsonBuilder()
        .serializeNulls()
        .excludeFieldsWithoutExposeAnnotation();
    // .registerTypeAdapter(Node.class, new NodeSerilizer());
    // .registerTypeAdapter(Graph.class, new GraphSerializer(this));
    // .registerTypeAdapter(Graph.class, new GraphDeserializer(this));
    if (debug_mode) {
      builder.setPrettyPrinting();
    }
    gson = builder.create();
  }

  public JsonFactory() {
    gson = new GsonBuilder()
        .serializeNulls()
        .excludeFieldsWithoutExposeAnnotation()
        // .registerTypeAdapter(Node.class, new NodeSerilizer())
        // .registerTypeAdapter(Graph.class, new GraphSerializer(this))
        // .registerTypeAdapter(Graph.class, new GraphDeserializer(this))
        .create();
  }

  public void toFile(Graph graph, String file_name) {

    // Open the file
    Writer writer = null;
    try {
      writer = new FileWriter(file_name);
    } catch (IOException e) {
      e.printStackTrace();
    }

    graph.buildBeforeJsonExport();
    gson.toJson(graph, Graph.class, writer);

    // Close the file
    try {
      if (writer != null)
        writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Graph fromFile(String file_name) {

    Reader reader = null;
    try {
      reader = new FileReader(file_name);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    // Read a file
    Graph graph = gson.fromJson(reader, Graph.class);
    graph.buildAfterJsonExport();
    graph.check();

    // Close the file
    try {
      if (reader != null)
        reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return graph;
  }
}
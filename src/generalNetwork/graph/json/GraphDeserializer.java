package generalNetwork.graph.json;

import generalNetwork.graph.Graph;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class GraphDeserializer implements JsonDeserializer<Graph> {

  private JsonFactory json;

  public GraphDeserializer(JsonFactory json) {
    this.json = json;
  }

  @Override
  public Graph deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context) throws JsonParseException {

    Graph graph = new Graph();
    // Parsing will be done here.
    JsonObject jsonObject = json.getAsJsonObject();
    
    // We retrieve the nb_nodes
    JsonElement el = jsonObject.get("nb_nodes");
    if (el == null) {
      System.out.println("The input json file does not contains the field nb_nodes");
      System.exit(1);
    }
    int nb_nodes = el.getAsInt();
    
    // We retrieve the nb of links
    el = jsonObject.get("nb_links");
    if (el == null) {
      System.out.println("The input json file does not contains the field nb_links");
      System.exit(1);
    }
    int nb_links = el.getAsInt();
    
    Node[] nodes = new Node[nb_nodes];
    Link[] links = new Link[nb_links];
    
    // We retrieve the links
    
    // We retrieve the nodes
    return null;
  }
}
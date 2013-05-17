package generalNetwork.graph.json;

import generalNetwork.graph.Graph;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;

import java.lang.reflect.Type;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GraphSerializer implements JsonSerializer<Graph> {

  private JsonFactory json;

  public GraphSerializer(JsonFactory json) {
    this.json = json;
  }

  @Override
  public JsonElement serialize(Graph graph, Type type,
      JsonSerializationContext context) {

    JsonObject result = new JsonObject();
    result.add("nb_nodes", new JsonPrimitive(graph.intersections.length));
    result.add("nb_links", new JsonPrimitive(graph.links.length));
    result.add("nodes", serializeNodesArray(graph.intersections));
    result.add("links", serializeLinksArray(graph.links));

    JsonArray nodes = new JsonArray();
    JsonObject json_node;
    Node node;
    for (int i = 0; i < graph.intersections.length; i++) {
      node = graph.intersections[i];
      json_node = new JsonObject();
      json_node.add("unique_id", new JsonPrimitive(node.unique_id));

    }
    return result;
  }

  /**
   * @brief Serialize the full information of a given link
   */
  private JsonObject serializeLink(Link link) {
    JsonObject json_node;
    json_node = (JsonObject) json.gson.toJsonTree(link);
    return json_node;
  }

  /**
   * @brief Serialize a node characteristics
   */
  private JsonObject serializeNode(Node node) {
    JsonObject json_node;
    json_node = (JsonObject) json.gson.toJsonTree(node);
    return json_node;
  }

  private JsonArray serializeNodesArray(Node[] nodes) {
    JsonArray json_nodes = new JsonArray();
    for (int i = 0; i < nodes.length; i++) {
      json_nodes.add(serializeNode(nodes[i]));
    }

    return json_nodes;
  }

  private JsonArray serializeLinksArray(Link[] links) {
    JsonArray json_nodes = new JsonArray();
    for (int i = 0; i < links.length; i++) {
      json_nodes.add(serializeLink(links[i]));
    }

    return json_nodes;
  }

  private JsonArray serializeNodesVector(Vector<Node> nodes) {
    JsonArray json_nodes = new JsonArray();
    for (int i = 0; i < nodes.size(); i++) {
      json_nodes.add(new JsonPrimitive(nodes.get(i).unique_id));
    }

    return json_nodes;
  }

  private JsonArray serializeLinksVector(Vector<Link> links) {
    JsonArray json_nodes = new JsonArray();
    for (int i = 0; i < links.size(); i++) {
      json_nodes.add(new JsonPrimitive(links.get(i).unique_id));
    }

    return json_nodes;
  }
}
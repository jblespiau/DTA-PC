package generalNetwork.graph.json;

import generalNetwork.graph.Graph;
import generalNetwork.graph.Link;
import generalNetwork.graph.Node;

import java.lang.reflect.Type;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @brief Can me completed to to modify the serialization/deserialization of a
 *        graph.
 * @details To be done it is needed to modify serialize and deserialize and to
 *          add in the JsonFactory .registerTypeAdapter (Graph.class, new
 *          GraphSerializer(this)
 * 
 */
public class GraphSerializer implements JsonSerializer<Graph>,
    JsonDeserializer<Graph> {

  private JsonFactory json;

  public GraphSerializer(JsonFactory json) {
    this.json = json;
  }

  @Override
  public JsonElement serialize(Graph graph, Type type,
      JsonSerializationContext context) {

    /*
     * JsonObject result = new JsonObject();
     * result.add("nb_nodes", new JsonPrimitive(graph.intersections.length));
     * result.add("nb_links", new JsonPrimitive(graph.links.length));
     * result.add("nodes", serializeNodesArray(graph.intersections));
     * result.add("links", serializeLinksArray(graph.links));
     * 
     * JsonArray nodes = new JsonArray();
     * JsonObject json_node;
     * Node node;
     * for (int i = 0; i < graph.intersections.length; i++) {
     * node = graph.intersections[i];
     * json_node = new JsonObject();
     * json_node.add("unique_id", new JsonPrimitive(node.unique_id));
     * 
     * }
     * return result;
     */
    return null;
  }

  @Override
  public Graph deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context) throws JsonParseException {

    /*
     * Graph graph = new Graph();
     * // Parsing will be done here.
     * JsonObject jsonObject = json.getAsJsonObject();
     * 
     * // We retrieve the nb_nodes
     * JsonElement el = jsonObject.get("nb_nodes");
     * if (el == null) {
     * System.out
     * .println("The input json file does not contains the field nb_nodes");
     * System.exit(1);
     * }
     * int nb_nodes = el.getAsInt();
     * 
     * // We retrieve the nb of links
     * el = jsonObject.get("nb_links");
     * if (el == null) {
     * System.out
     * .println("The input json file does not contains the field nb_links");
     * System.exit(1);
     * }
     * int nb_links = el.getAsInt();
     * 
     * Node[] nodes = new Node[nb_nodes];
     * Link[] links = new Link[nb_links];
     * 
     * // We retrieve the links
     * 
     * // We retrieve the nodes
     */
    return null;
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
      json_nodes.add(new JsonPrimitive(nodes.get(i).getUnique_id()));
    }

    return json_nodes;
  }

  private JsonArray serializeLinksVector(Vector<Link> links) {
    JsonArray json_nodes = new JsonArray();
    for (int i = 0; i < links.size(); i++) {
      json_nodes.add(new JsonPrimitive(links.get(i).getUnique_id()));
    }

    return json_nodes;
  }
}
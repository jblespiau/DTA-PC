package generalNetwork.graph.json;

import generalNetwork.graph.Node;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class NodeSerilizer implements JsonSerializer<Node> {

  @Override
  public JsonElement serialize(Node node, Type type,
      JsonSerializationContext context) {

    JsonObject result = new JsonObject();
    result.add("unique_id", new JsonPrimitive(node.getUnique_id()));

    JsonArray incoming = new JsonArray();
    for (int i = 0; i < node.incoming_links.size(); i++) {
      incoming
          .add(new JsonPrimitive(node.incoming_links.get(i).getUnique_id()));
    }
    result.add("incoming", incoming);

    JsonArray outgoing = new JsonArray();
    for (int i = 0; i < node.outgoing_links.size(); i++) {
      outgoing
          .add(new JsonPrimitive(node.outgoing_links.get(i).getUnique_id()));
    }
    result.add("outgoing", outgoing);

    return result;
  }

  /*
   * @Override
   * public Node deserialize(JsonElement json, Type type,
   * JsonDeserializationContext context) throws JsonParseException {
   * 
   * JsonObject jsonObject = json.getAsJsonObject();
   * JsonElement id_el = jsonObject.get("nb_nodes");
   * int id = id_el.getAsInt();
   * Iterator<JsonElement> iterator =
   * jsonObject.get("nb_nodes").getAsJsonArray().iterator();
   * 
   * JsonElement outgoing_el = jsonObject.get("nb_nodes");
   * 
   * Node node = new Node();
   * 
   * return node;
   * }
   */
}

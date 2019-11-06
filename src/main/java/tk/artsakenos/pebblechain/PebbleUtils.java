/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tk.artsakenos.pebblechain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrea
 */
public class PebbleUtils {

    public static String toJson(Pebble pebble) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pebble);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Pebble.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Per ora deserializzo così perché voglio mantenere alcuni attributi final.
     *
     * @param json
     * @return
     * @throws PebbleException
     */
    public static Pebble fromJson(String json) throws PebbleException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(json);
        } catch (IOException ex) {
            return null;
        }
        LinkedList<String> links = new LinkedList<>();
        Iterator<JsonNode> elements = node.get("links_previous").elements();
        while (elements.hasNext()) {
            links.add(elements.next().asText());
        }
        final Pebble pebble = new Pebble(
                node.get("hash_previous").asText(),
                links.toArray(new String[links.size()]),
                node.get("prefix").asText(),
                node.get("owner").asText(),
                node.get("data").asText(),
                node.get("hash_current").asText(),
                node.get("created_epoch").asLong(),
                node.get("nonce").asInt());
        return pebble;
    }
}

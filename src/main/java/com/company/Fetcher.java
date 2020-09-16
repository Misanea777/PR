package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Fetcher {
    public static List<DataBlock> dataBlocks = Main.dataBlocks;
    public static Iterator<JsonNode> fetch(String path, String Jbody) throws JsonProcessingException {
        ObjectNode node = new ObjectMapper().readValue(Jbody, ObjectNode.class);
        Iterator<JsonNode> links = Collections.emptyIterator();
        // Fech the data
        if (node.has("data")) {
            dataBlocks.add(new DataBlock(path, node.get("data").toString()));
        }
        //Fech the links
        if (node.has("link")) {
            links = node.get("link").elements();
        }
        return links;
    }
}

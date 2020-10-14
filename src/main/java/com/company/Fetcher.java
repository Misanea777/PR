package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Fetcher {
    private static List<DataBlock> dataBlocks = new ArrayList<DataBlock>();
    private static JSONArray mergedData = new JSONArray();

    public static List<DataBlock> getDataBlocks() {
        return dataBlocks;
    }

    public static Iterator<JsonNode> fetch(String path, String Jbody) throws JsonProcessingException {
        ObjectNode node = new ObjectMapper().readValue(Jbody, ObjectNode.class);
        Iterator<JsonNode> links = Collections.emptyIterator();
        // Fech the data
        if (node.has("data")) {
            dataBlocks.add(new DataBlock(path, node.get("data").asText(), String.valueOf(node.get("mime_type"))));
        }
        //Fech the links
        if (node.has("link")) {
            links = node.get("link").elements();
        }
        return links;
    }

    public static void printDataBlocks() {
        for (DataBlock dataBlock: dataBlocks) {
            System.out.println(dataBlock.getType());
            System.out.println(dataBlock.getData());
            System.out.println("_________________");
            System.out.println(dataBlock.getConvertedData());
            System.out.println();
        }
    }

    public static void merge() {
        for (DataBlock dataBlock: dataBlocks) {
            for(int i=0; i<dataBlock.getConvertedData().length(); i++) {
                mergedData.put((JSONObject) dataBlock.getConvertedData().get(i));
            }
        }
        for(int i=0; i<mergedData.length(); i++) {
            mergedData.getJSONObject(i).put("GLOBAL_ID", getId(mergedData.getJSONObject(i)));
            System.out.println(mergedData.getJSONObject(i));
        }

    }

    public static String getId(JSONObject field) {
        if(field.opt("email") != null) return field.opt("email").toString();
        if(field.opt("first_name") != null && field.opt("last_name") != null) return field.opt("first_name").toString() + " " + field.opt("last_name").toString();

        return null;
    }


    public static JSONArray getFieldFromColumnAndId(String columnName, String id) {
        JSONArray repsonse = new JSONArray();
        
        for(int i=0; i<mergedData.length(); i++) {
            if(mergedData.getJSONObject(i).opt(columnName) == null) continue;
            if(mergedData.getJSONObject(i).opt(columnName).toString().toLowerCase().contains(id.toLowerCase())) repsonse.put(mergedData.getJSONObject(i));
        }
        return repsonse;
    }

    public static String getColumn(String column) {
        String response = "";

        JSONObject tmp;
        for(int i=0; i<mergedData.length(); i++) {
            tmp = mergedData.getJSONObject(i);
            if(tmp.opt(column) == null) continue;
            response += "$Id : \"" + tmp.opt("GLOBAL_ID").toString() + "\" "
                    + "$Column : \"" + column + "\" --> "
                    + tmp.opt(column).toString() + "||";
        }
        return response;
    }

    public static String getFromColumn(String column, String name) {
        String response = "";

        JSONObject tmp;
        for(int i=0; i<mergedData.length(); i++) {
            tmp = mergedData.getJSONObject(i);
            if(tmp.opt(column) == null) continue;

            if(tmp.opt(column).toString().equals(name)) {
                Iterator<String> keys = tmp.keys();
                while(keys.hasNext()) {
                    String next = keys.next();
                    response += "$" + next + " : \"" + tmp.opt(next).toString() + "\"||";
                }
                response += "||";
            }
        }
        return response;

    }
}

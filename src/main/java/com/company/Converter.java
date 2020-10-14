package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Converter {

    private Converter(){}

    public static JSONArray converStringtToJson(String type, String  data) throws IOException {
        if(type.equals("application/x-yaml")) return fromYamlStringToJson(data);
        if(type.equals("Json")) return fromJsonStringToJson(data);
        if(type.equals("text/csv")) return fromCsvStringToJson(data);
        if(type.equals("application/xml")) return fromXmlStringToJson(data);

        return null;
    }

    public static JSONArray fromJsonStringToJson(String data) {
        return new JSONArray(data);
    }

    public static JSONArray fromXmlStringToJson(String data) {
        return XML.toJSONObject(data).getJSONObject("dataset").getJSONArray("record");
    }

    public static JSONArray fromCsvStringToJson(String data) throws IOException {
        CsvSchema csv = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(data);
        List<Map<?, ?>> list = mappingIterator.readAll();
        return new JSONArray(list);
    }

    public static JSONArray fromYamlStringToJson(String data) throws JsonProcessingException {
        Object obj = new ObjectMapper(new YAMLFactory()).readValue(data, Object.class);
        String res = new ObjectMapper().writeValueAsString(obj);
        return new JSONArray(res);
    }

}

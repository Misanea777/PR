package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import javax.swing.text.html.HTMLDocument;

public class DataBlock {
    private String URI_PATH;
    private String data;
    private String type;
    private JSONArray convertedData;


    public DataBlock(String URI_PATH, String data, String type) {
        this.URI_PATH = URI_PATH;
        this.data = data;
        this.type = (type != "null")? type.substring(1, type.length()-1) : "Json";
        convertData();
    }

    private void convertData() {
        try {
            this.convertedData = Converter.converStringtToJson(this.type, this.data);
        } catch (IOException e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(e);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
        }

    }

    public String getURI_PATH() {
        return URI_PATH;
    }

    public String getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    public JSONArray getConvertedData() {
        return convertedData;
    }

    @Override
    public String toString() {
        return "DataBlock{" +
                "URI_PATH='" + URI_PATH + '\'' +
                ", data='" + data + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    private static enum DATA_TYPE {
        JSON, YAML, CSV, XML
    }

}

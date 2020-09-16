package com.company;

public class DataBlock {
    private String URI_PATH;
    private String data;

    public DataBlock(String URI_PATH, String data) {
        this.URI_PATH = URI_PATH;
        this.data = data;
    }

    public String getURI_PATH() {
        return URI_PATH;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DataBlock{" +
                "URI_PATH='" + URI_PATH + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

}

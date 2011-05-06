package com.lightboxtechnologies.spectrum;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Writable;

import org.json.simple.*;
import org.json.simple.parser.*;

public class JsonWritable implements Writable {
  public static interface JsonDispatch {
    public String write(Object o);
  }

  public static class MapDispatch implements JsonDispatch {
    public String write(Object o) {
      return JSONObject.toJSONString((Map)o);
    }
  }

  public static class ListDispatch implements JsonDispatch {
    public String write(Object o) {
      return JSONArray.toJSONString((List)o);
    }
  }

  private final JSONParser Parser = new JSONParser();
  private JsonDispatch Writer;
  private Object Data;
  private String Json;

  private static final MapDispatch MapDisp = new MapDispatch();
  private static final ListDispatch ListDisp = new ListDispatch();

  public void set(List l) {
    Data = l;
    Writer = ListDisp;
    Json = null;
  }

  public void set(Map m) {
    Data = m;
    Writer = MapDisp;
    Json = null;
  }

  public Object get() {
    // Lazily parses the JSON string; we don't pay the parse tax
    // with just reading and writing
    try {
      Data = Parser.parse(Json);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    Writer = Data instanceof Map ? MapDisp: ListDisp;
    return Data;
  }

  public void readFields(DataInput in) throws IOException {
    Json = in.readUTF();
  }

  public void write(DataOutput out) {
    try {
      out.writeUTF(Json == null ? Writer.write(Data): Json); // if we already have the serialized form, use it
    }
    catch (IOException err) {
      throw new RuntimeException(err);
    }
  }

  public String toString() {
    return Json == null ? (Data != null ? Writer.write(Data): ""): Json;
  }
}
package com.PVZ.database;

public class DatabaseEntry {
      private String className;
      private String jsonData;
      
      public DatabaseEntry() {}
      public DatabaseEntry(String className, String jsonData) {
            this.className = className;
            this.jsonData = jsonData;
      }
      
      public String getClassName() { return className; }
      public String getJsonData() { return jsonData; }
}

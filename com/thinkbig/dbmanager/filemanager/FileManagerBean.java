package com.thinkbig.dbmanager.filemanager;

import java.util.ArrayList;

public class FileManagerBean {

    private String database;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    private ArrayList<String> extendedTableNames = new ArrayList<String>();

    public ArrayList<String> getExtendedTableNames() {
        return extendedTableNames;
    }

    public void setExtendedTableNames(String tableName) {
        this.extendedTableNames.add(tableName);
    }

    private ArrayList<String> tableNames = new ArrayList<String>();

    public ArrayList<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(String tableName) {
        this.tableNames.add(tableName);
    }
}

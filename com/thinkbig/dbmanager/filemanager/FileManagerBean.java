package com.thinkbig.dbmanager.filemanager;

import java.io.File;
import java.util.ArrayList;

public class FileManagerBean {

    private String database;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    private ArrayList<File> extendedTableNames = new ArrayList<File>();

    public ArrayList<File> getExtendedTableNames() {
        return extendedTableNames;
    }

    public void setExtendedTableNames(File fileName) {
        this.extendedTableNames.add(fileName);
    }

    private ArrayList<String> tableNames = new ArrayList<String>();

    public ArrayList<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(String tableName) {
        this.tableNames.add(tableName);
    }
}

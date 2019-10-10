package com.thinkbig.dbmanager.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.thinkbig.dbmanager.DBManagerBean;
import com.thinkbig.dbmanager.jdbcmanager.JDBCMananger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class FileManager {

    private final DBManagerBean dbBean;
    FileManagerBean fileBean = null;
    JDBCMananger jdbcMananger = null;
    public FileManager(DBManagerBean dbBean)
    {
        fileBean = new FileManagerBean();
        jdbcMananger = new JDBCMananger();
        this.dbBean = dbBean;
    }
    // read files one by one and create tables and database

    /**
     *
     * @param files
     * @param excludeFileList
     */
    public void parseFiles(File[] files, ArrayList<String> excludeFileList)
    {
        for(File file : files)
        {
            if(!excludeFileList.contains(file.getName()))
            {
                try {
                    String jsonFile = new String(Files.readAllBytes(file.toPath()));
                    JSONObject fileObject = new JSONObject(jsonFile);
                    if(fileBean.getDatabase() == null) // to avoid creating database if already created
                    {
                        jdbcMananger.getDBConnection(dbBean);
                        //method to create database
                        createDatabase(fileObject.get("database").toString());
                        fileBean.setDatabase(fileObject.get("database").toString());
                        dbBean.setDatabase(fileObject.get("database").toString());
                        jdbcMananger.close();
                        //connecting to Database created
                        jdbcMananger.getDBConnection(dbBean);
                    }
                    if(fileObject.has("extends")) // to skip tables that have foreign key references
                    {
                        fileBean.setExtendedTableNames(file.getName());
                    }
                    else {
                        // create table in database created.
                        createTable(fileObject);
                        fileBean.setTableNames(file.getName());
                    }
                } catch (IOException | JSONException e) {
                    System.out.println("Exception occured while reading JSON File : "+file.getName()+" : ");
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     *
     * @param fileObject
     * @throws JSONException
     */
    private void createTable(JSONObject fileObject) throws JSONException
    {
        String tableName = fileObject.get("table_name").toString();
        JSONArray columns = (JSONArray) fileObject.get("columns");
        String columnQuery = "CREATE TABLE "+tableName+" (";
        String constraint = "";
        String primaryconstraint = "PRIMARY KEY(";
        String uniqueConstraint = "UNIQUE(";
        // To construct all the constraints specified for column.
        for(int i= 0 ; i< columns.length(); i++)
        {
            constraint = "";
            JSONObject columnObject = (JSONObject) columns.get(i);
            columnQuery = columnQuery+columnObject.get("name")+" "+columnObject.get("type");

            if(columnObject.has("constraints")) {
                JSONObject constraints = (JSONObject) columnObject.get("constraints");
                constraint = constructConstraint(constraints);
                if(constraints.has("primary_key"))
                {
                    primaryconstraint += contructPrimaryConstraint(constraints, columnObject.get("name").toString(),primaryconstraint);
                }
                if(constraints.has("unique"))
                {
                    uniqueConstraint += constructUniqueConstraint(constraints,columnObject.get("name").toString(),uniqueConstraint);
                }
            }
            constraint += ",";
            columnQuery += constraint;
            //System.out.println("---- constraint ---- "+constraint);
        }
        columnQuery += uniqueConstraint+")"+", "+primaryconstraint+")"+")"; //primary constraint added at last
        System.out.println("---- columnQuery ----"+columnQuery);
        jdbcMananger.executeSQLUpdate(columnQuery);
    }

    /**
     *
     * @param constraints
     * @param columnName
     * @return
     * @throws JSONException
     */
    private String constructUniqueConstraint(JSONObject constraints, String columnName, String uniqueConstraint) throws JSONException {
        String constraint = "";
        if(!uniqueConstraint.equalsIgnoreCase("UNIQUE("))
            constraint = ",";
        if(constraints.has("unique")){
            constraint += " "+(constraints.get("unique").equals(true)? columnName:" ");
        }
        return constraint;
    }

    /**
     *
     * @param constraints
     * @param columnName
     * @return
     * @throws JSONException
     */
    private String contructPrimaryConstraint(JSONObject constraints, String columnName, String primaryConstraint) throws JSONException {
        String constraint = "";
        if(!primaryConstraint.equalsIgnoreCase("PRIMARY KEY("))
            constraint = ",";
        if(constraints.has("primary_key")){
            constraint += " "+(constraints.get("primary_key").equals(true)? columnName:" ");
        }
        return constraint;
    }

    /**
     *
     * @param constraints
     * @return
     * @throws JSONException
     */
    private String constructConstraint(JSONObject constraints) throws JSONException
    {
        String constraint =  "";
            if (constraints.has("null")) {
                constraint += " "+(constraints.get("null").equals(true) ? "" : "NOT NULL");
            }
            if(constraints.has("default"))
            {
                constraint += " "+"DEFAULT ("+constraints.get("default")+")";
            }
        return constraint;
    }
    //create database

    /**
     *
     * @param database
     */
    private void createDatabase(String database) {
        String query = "CREATE DATABASE "+database;
        jdbcMananger.executeSQLUpdate(query);
    }
}

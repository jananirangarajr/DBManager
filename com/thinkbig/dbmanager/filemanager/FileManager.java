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
     *This method reads the json file in specified directory and creates table
     * @param files
     * @param excludeFileList
     */
    public void parseFiles(File[] files, ArrayList<String> excludeFileList)
    {
        //create database
        createDatabase(dbBean.getDatabase());
        fileBean.setDatabase(dbBean.getDatabase());
        for(File file : files)
        {
            if(!excludeFileList.contains(file.getName()))
            {
                try {
                    String jsonFile = new String(Files.readAllBytes(file.toPath()));
                    JSONObject fileObject = new JSONObject(jsonFile);
                    //connecting to Database created
                    jdbcMananger.getDBConnection(dbBean);
                    if(fileObject.has("extends")) // to skip tables that have foreign key references
                    {
                        String[] extendedTablesList = fileObject.get("extends").toString().split(",");
                        if(checkTableNameExists(extendedTablesList)) // to check if the extended table already exists and create dependency table
                        {
                            createTable(fileObject);
                        }
                        else {
                            fileBean.setExtendedTableNames(file);
                        }
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
        //create table which has foreign key references
        createTableWithFkReference();
    }

    /**
     * checks wheather all the dependency tables exists in database.If all dependency table exists return true.
     * @param extendedTablesList
     * @return
     */
    private boolean checkTableNameExists(String[] extendedTablesList) {
        for(int i = 0; i < extendedTablesList.length ; i++)
        {
            if(!jdbcMananger.tableExists(extendedTablesList[i]))
                return false;
        }
        return true;
    }

    /**
     *Create table which has foreign key references.
     */

    private void createTableWithFkReference()  {
        ArrayList<File> files = (ArrayList<File>) fileBean.getExtendedTableNames().clone();
        for(File file : files)
        {
            String jsonFile = null;
            try {
                jsonFile = new String(Files.readAllBytes(file.toPath()));
                JSONObject fileObject = new JSONObject(jsonFile);
                if(fileObject.has("extends")) // to skip tables that have foreign key references
                {
                    String[] extendedTablesList = fileObject.get("extends").toString().split(",");
                    if(checkTableNameExists(extendedTablesList))
                    {
                        createTable(fileObject);
                        fileBean.getExtendedTableNames().remove(file);
                    }
                }
            } catch (IOException | JSONException e) {
                System.out.println("Exception occured while reading JSON File : "+file.getName()+" : ");
                e.printStackTrace();
            }
        }
        if(!fileBean.getExtendedTableNames().isEmpty())
            createTableWithFkReference();
    }

    /**
     *Reads the Json file and construct the Query with all the constraints specified. Executes the Query and create the table in DATABASE specified.
     * @param fileObject - json file
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
        String fkConstraint = "";
        // To construct all the constraints specified for column.
        for(int i= 0 ; i< columns.length(); i++)
        {
            constraint = "";
            JSONObject columnObject = (JSONObject) columns.get(i);
            columnQuery = columnQuery+columnObject.get("name")+" "+columnObject.get("type");

            if(columnObject.has("constraints")) {
                JSONObject constraintsObject = (JSONObject) columnObject.get("constraints");
                constraint = constructConstraint(constraintsObject);
                if(constraintsObject.has("primary_key"))
                {
                    primaryconstraint += contructPrimaryConstraint(constraintsObject, columnObject.get("name").toString(),primaryconstraint);
                }
                if(constraintsObject.has("unique"))
                {
                    uniqueConstraint += constructUniqueConstraint(constraintsObject,columnObject.get("name").toString(),uniqueConstraint);
                }
                if(constraintsObject.has("foreign_key"))
                {
                    String foreignKeyValue = constraintsObject.get("foreign_key").toString();
                    foreignKeyValue = foreignKeyValue.substring(0,foreignKeyValue.indexOf('.'))+"("+foreignKeyValue.substring((foreignKeyValue.indexOf('.')+1),(foreignKeyValue.length()))+")";
                    fkConstraint += "FOREIGN KEY ("+columnObject.get("name")+") REFERENCES "+foreignKeyValue;
                    if(constraintsObject.has("cascade"))
                    {
                        fkConstraint += " "+constraintsObject.get("cascade")+" cascade";
                    }
                    fkConstraint += ",";
                }
            }
            constraint += ",";
            columnQuery += constraint;
            //System.out.println("---- constraint ---- "+constraint);
        }
        primaryconstraint = primaryconstraint.equals("PRIMARY KEY(")?"":primaryconstraint+")";//assign empty string if there is no primary key.
        columnQuery += primaryconstraint.equals("")?"":primaryconstraint+","; //append ',' only if primary key exists

        uniqueConstraint = uniqueConstraint.equals("UNIQUE(")?"":uniqueConstraint+")";//assign empty string if there is no uniue key.
        columnQuery += uniqueConstraint.equals("")?"":uniqueConstraint+","; //append ',' only if unique key exists

        columnQuery += fkConstraint.equals("")?"":fkConstraint.substring(0,fkConstraint.length()-1); //don't append ',' for fk as this is the last one to append

        columnQuery = fkConstraint.equals("")?columnQuery.substring(0,columnQuery.length()-1)+")":columnQuery+")";// trim the trailing ',' in columnQuery only if there is no fk as we don't append ',' for FK.[if ', is appended for fk also, need to remove 2','char if fk not exsists']
        System.out.println("---- columnQuery ----"+columnQuery);

        jdbcMananger.executeSQLUpdate(columnQuery);
    }

    /**
     *construct unique columns query.
     * @param constraints
     * @param columnName
     * @return String for unique query
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
     * construct primary columns query
     * @param constraints
     * @param columnName
     * @return string for primary part
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
     * construct query for constraints specified in json files
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

    /**
     * creates a database
     * @param database
     */
    private void createDatabase(String database) {
        jdbcMananger.getDBConnection(dbBean);
        String query = "CREATE DATABASE "+database;
        jdbcMananger.executeSQLUpdate(query);
        jdbcMananger.close();
        JDBCMananger.isDatabaseCreated = true;
    }
}

package com.thinkbig.dbmanager;

public class DBManagerBean {
    static private String fileLoc;
    static private String db;

    static private String userName;
    static private char[] password;

    static private int port;
    static private String ip;
    static private String database;


    public String getDb() {
        return db;
    }

    public static void setDb(String db) {
        DBManagerBean.db = db;
    }

    public int getPort() {
        return port;
    }

    public static void setPort(int port) {
        DBManagerBean.port = port;
    }

    public String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        DBManagerBean.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        DBManagerBean.userName = userName;
    }

    public char[] getPassword() {
        return this.password;
    }

    public static void setPassword(char[] password) {
        DBManagerBean.password = password;
    }

    public String getFileLoc() {
        return fileLoc;
    }

    public static void setFileLoc(String fileLoc) {
        DBManagerBean.fileLoc = fileLoc;
    }

    public String getDatabase() {
        return database;
    }

    public static void setDatabase(String database) {
        DBManagerBean.database = database;
    }
}

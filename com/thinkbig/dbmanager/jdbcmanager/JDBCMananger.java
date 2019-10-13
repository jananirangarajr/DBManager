package com.thinkbig.dbmanager.jdbcmanager;

import com.thinkbig.dbmanager.DBManagerBean;

import java.sql.*;

public class JDBCMananger {
    JDBCBean bean = null;
    public static boolean isDatabaseCreated = false;
    public JDBCMananger()
    {
        bean = new JDBCBean();
    }

    /**
     *
     * @param serverURL
     * @param driverName
     * @param userDetails
     * @return
     */
    private void getDBConnection(String serverURL, String driverName, String userDetails) {
        Connection connection = null;
        Statement statement = null;
        try
        {
            Class.forName(driverName);
            connection = DriverManager.getConnection(serverURL,String.valueOf(swapArray(userDetails.substring(0,userDetails.indexOf('#')).toCharArray())),String.valueOf(swapArray(userDetails.substring(userDetails.indexOf('#')+3).toCharArray())));
            statement = connection.createStatement();
            bean.setStatement(statement);
            bean.setConnection(connection);
        }
        catch(Exception exception) {
            System.out.println("Exception Occured while getting DBConnection : ");
            exception.printStackTrace();
            closeConnection();
        }
    }
    //getConnection

    /**
     *
     * @param dbBean
     */
    public void getDBConnection(DBManagerBean dbBean) {
        String driverName = null;
        String serverURL = null;
        if(dbBean.getDb().equalsIgnoreCase("psql") || dbBean.getDb().equalsIgnoreCase("pgsql") || dbBean.getDb().equalsIgnoreCase("postgres")) {
            driverName = "org.postgresql.Driver";
            serverURL = "jdbc:postgresql://"+dbBean.getIp()+":"+dbBean.getPort()+"/";
        }
        else if(dbBean.getDb().equalsIgnoreCase("Mssql")) {
            driverName = "com.mysql.jdbc.Driver";
            serverURL = "jdbc:mysql://"+dbBean.getIp()+":"+dbBean.getPort()+"/";
        }
        if(isDatabaseCreated) {
            serverURL += dbBean.getDatabase();
        }
        String userDetails = getEncryptedUserData(dbBean);
        getDBConnection(serverURL,driverName,userDetails);
    }

    /**
     *
     * @param dbBean
     * @return
     */
    //method used to encrypt userName and password(basic implementation)
    private String getEncryptedUserData(DBManagerBean dbBean) {
        String userDetails = null;
        char[] userName = dbBean.getUserName().toCharArray();
        userName = swapArray(userName);
        char [] password = dbBean.getPassword().clone();
        password = swapArray(password);
        userDetails = String.valueOf(userName)+"###"+String.valueOf(password);
        return  userDetails;
    }

    /**
     *
     * @param swapArray
     * @return
     */
    private char[] swapArray(char[] swapArray)
    {
        int startIndex = 0;
        int size = swapArray.length;
        for (int i = 0; i < size / 2; i++) {
            if (size % 2 != 0 && i == size / 2)
                break;
            char temp = swapArray[startIndex];
            swapArray[startIndex] = swapArray[size-1];
            swapArray[size-1] = temp;
            startIndex++;
            size--;
        }
        return swapArray;
    }

    /**
     *
     * @param query
     */
    //method to execute selectQuery
    public void executeSQL(String query)
    {
        executeSQLStatement(query);
    }

    /**
     *
     * @param query
     */
    private void executeSQLStatement(String query)
    {
        Statement statement = bean.getStatement();
        try (ResultSet resultSet = statement.executeQuery(query)) {

        }
        catch (SQLException sqlException)
        {
            System.out.println("Error while executing Query : ");
            sqlException.printStackTrace();
        }

    }

    /**
     *
     * @param query
     */
    //method to execute update query
    public void executeSQLUpdate(String query)
    {
        executeUpdateStatement(query);
    }

    /**
     *
     * @param query
     */
    private void executeUpdateStatement(String query)
    {
        Statement statement = bean.getStatement();
        try{
            statement.execute(query);
        }
        catch (SQLException sqlException)
        {
            System.out.println("Error while executing Query : ");
            sqlException.printStackTrace();
            closeConnection();
        }
    }
    /**
     *
     */
    public void close() {
        closeConnection();
    }
    /**
     *
     */
    //method to close connection
    private void closeConnection()
    {
        if (bean.getConnection() != null) {
            try {
                bean.getConnection().close();
            } catch (SQLException e) {
                System.out.println("Exception while closing connection : ");
                e.printStackTrace();
            }
        }
        if (bean.getStatement() != null) {
            try {
                bean.getStatement().close();
            } catch (SQLException e) {
                System.out.println("Exception while closing statememt : ");
                e.printStackTrace();
            }
        }
    }
    public boolean tableExists(String tableName)
    {
        DatabaseMetaData dbm = null;
        try {
            dbm = bean.getConnection().getMetaData();
            // check if table is there
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                // Table exists
                return true;
            }
            else {
                // Table does not exist
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Exception while getting Metadata : ");
            e.printStackTrace();
        }
        return false;
    }
}


package com.thinkbig.dbmanager;

import com.thinkbig.dbmanager.filemanager.FileManager;
import com.thinkbig.dbmanager.jdbcmanager.JDBCMananger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class DBManager {

    public static void main(String args[])
    {
        // Input form the user
        getUserInput();
        DBManagerBean dbBean = new DBManagerBean();
        /** logic starts here **/
        try
        {
            File dir = new File(dbBean.getFileLoc());
            if(dir.isDirectory())
            {
                File [] files = dir .listFiles(); // read all the files from the directory
                ArrayList<String> excludeFilesList = new ArrayList<String>();
                for(File file : files) {
                    //check for extension only to parse json files alone
                    boolean isJsonExtension = file.getName().endsWith(".json");
                    if(!isJsonExtension) {
                        excludeFilesList.add(file.getName());
                    }
                }
               new FileManager(dbBean).parseFiles(files,excludeFilesList);
            }
            else
            {
                throw new FileNotFoundException();
            }

        }
        catch(FileNotFoundException fileException)
        {
            System.out.println("Unable to find the directory or file. Please provide a valid directory : ");
            fileException.printStackTrace();
        }
        catch(Exception exception)
        {
            System.out.println("Exception : ");
            exception.printStackTrace();
        }
        finally
        {
            new JDBCMananger().close();
        }
    }

    /**
     *Method to get input Data for json files directory and DB Details
     */
    private static void getUserInput() {
        System.out.print("Enter the json files location : ");
        Scanner sc = new Scanner(System.in);
        DBManagerBean.setFileLoc(sc.next());
        System.out.println("Enter the DB details : ");
        System.out.println("DB : ");
        DBManagerBean.setDb(sc.next());
        System.out.println("port : ");
        DBManagerBean.setPort(sc.nextInt());
        System.out.println("DB server URL");
        DBManagerBean.setIp(sc.next());
        System.out.println("DB UserName");
        DBManagerBean.setUserName(sc.next());
        DBManagerBean.setPassword(System.console().readPassword("DB password"));
        /*System.out.println("password");
        DBManagerBean.setPassword(sc.next().toCharArray());*/
        System.out.println("Database Name");
        DBManagerBean.setDatabase(sc.next());
    }
}

package com.thinkbig.dbmanager;

import com.thinkbig.dbmanager.filemanager.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.FileNameMap;
import java.util.ArrayList;
import java.util.Scanner;

public class main {
    static String fileLoc;
    static String db;
    static int port;
    static String ip;
    public static void main(String args[])
    {
        //Needed Input form the user
        System.out.print("Enter the json files location : ");
        Scanner sc = new Scanner(System.in);
        fileLoc = sc.next();
        System.out.println("Enter the DB details : ");
        System.out.println("DB : ");
        db = sc.next();
        System.out.println("port : ");
        port = sc.nextInt();
        System.out.println("DB server URL");
        ip = sc.next();

        /** logic starts here **/

        try
        {
            File dir = new File(fileLoc);
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
                    System.out.println("canonical path"+file.getCanonicalPath()+" "+file.getCanonicalFile());
                }
                new FileManager().parseFiles(files,excludeFilesList);
            }
            else
            {
                throw new FileNotFoundException();
            }

        }
        catch(FileNotFoundException fileException)
        {
            System.out.println("Unable to find the directory or file. Please provide a valid directory");
        }
        catch(Exception exception)
        {

        }
        finally
        {

        }


    }
}

package com.thinkbig.dbmanager.filemanager;

import java.io.File;
import java.util.ArrayList;

public class FileManager {

    FileManagerBean fileBean = null;
    public FileManager()
    {
        fileBean = new FileManagerBean();
    }

    public void parseFiles(File[] files, ArrayList<String> excludeFileList)
    {
        for(File file : files)
        {
            if(!excludeFileList.contains(file.getName()))
            {

            }
        }
    }
}

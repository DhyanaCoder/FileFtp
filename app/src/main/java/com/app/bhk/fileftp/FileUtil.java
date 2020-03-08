package com.app.bhk.fileftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {
    private static File saveFile=null;
    private static FTPClient ftpClient;

    public static FTPClient getFtpClient() {
        return ftpClient;
    }

    public static void setFtpClient(FTPClient ftpClient) {
        FileUtil.ftpClient = ftpClient;

    }

    public static File getSaveFile() {
        return saveFile;
    }

    public static void setSaveFile(File saveFile) {
        FileUtil.saveFile = saveFile;
    }

    public static void Copy(File file, File directory){
        FileOutputStream outputStream=null;
        FileInputStream inputStream=null;
        if(!directory.isDirectory()||!file.isFile()||file.getParentFile().equals(directory))
            return;
        try {
            File newFile=new File(directory,file.getName());
            if(!newFile.exists()){
                newFile.createNewFile();
            }
            outputStream=new FileOutputStream(newFile);
            inputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while((bytesRead=inputStream.read(buf))>0){
                outputStream.write(buf,0,bytesRead);
            }
            inputStream.close();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

package com.app.bhk.fileftp;

import android.content.Intent;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpUtil {
    private static FTPClient mClient;
    private static String hostname;
    private static int port=21;//端口号默认21
    private static String account;
    private static String password;
    private static boolean check;
    private static FTPFile lists[];
    private static StringBuffer CurrentFile=new StringBuffer();
    private static FTPFile ftpFile;

    public static FTPFile getFtpFile() {
        return ftpFile;
    }
    public static  void copyFtpFile(final FTPFile file, final MainActivity activity){
        //待完成。
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileAdapter adapter = (FileAdapter) activity.getAdapter(0);
                    downloadSingle(adapter.getCurrentFile(), file);


                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static void setFtpFile(FTPFile ftpFile) {
        FtpUtil.ftpFile = ftpFile;
    }
    public static  void deleteFtpFile(final FTPFile ftpFile){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    mClient.deleteFile(ftpFile.getName());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void LastDirectory(){
        //切换目录，没用的原因是多个子线程没法同步控制有待改进。思考可以用锁来实现留待后续。

        try {
            int i = CurrentFile.toString().lastIndexOf('/');
            if (i >= 0) {
                CurrentFile.delete(i, CurrentFile.length());
                boolean f = mClient.changeToParentDirectory();
                Log.d("test1234", " " + f + " " + CurrentFile);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void NextDirectoty(final FTPFile file){
        //切换目录，没用的原因是多个子线程没法同步控制有待改进。思考可以用锁来实现留待后续。
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CurrentFile.append("/" + file.getName());
                    boolean f = mClient.changeWorkingDirectory(CurrentFile.toString());
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
//    public static FTPFile[] GetDirectoryList(){
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    lists = mClient.listDirectories();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//
//            }
//        return lists;
//    }
//    public  static FTPFile[] GetFileList(){
//        }).start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    lists = mClient.listFiles();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        return lists;
//    }
    public static void Init(String hostname1,int port1,String account1,String password1){
        hostname=hostname1;
        port=port1;
        account=account1;
        password=password1;
        if(mClient==null) {
            mClient = new FTPClient();
        }
        if(mClient.isConnected()){
            try {
                mClient.disconnect();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
    public static FTPClient getmClient() {
        return mClient;
    }

    public static boolean Connect(){
        check=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mClient.isConnected()) {
                        mClient.connect(hostname,port);
                        boolean status = mClient.login(account, password);
                        if (status) {
                            check=true;
                            try {
                                mClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE);
                                // 使用被动模式设为默认
                                mClient.enterLocalPassiveMode();
                                // 二进制文件支持
                                mClient.setFileType(FTP.BINARY_FILE_TYPE);
                                //设置缓存
                                mClient.setBufferSize(1024);
                                //设置编码格式，防止中文乱码
                                //中文乱码问题后期再说吧
                                mClient.setControlEncoding("UTF-8");
                                //设置连接超时时间
                                mClient.setConnectTimeout(10 * 1000);
                                //设置数据传输超时时间
                                mClient.setDataTimeout(10 * 1000);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    check=false;
                }
            }

        }).start();
        return check;
    }
    /**
     * 上传.
     *
     * @param localFilePath 需要上传的本地文件路径

     * @return 上传结果
     * @throws IOException
     */
    public static boolean uploadFile(String localFilePath) throws IOException {
        boolean flag = false;

        // 二进制文件支持
        mClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        // 设置模式
        mClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);

        File localFile = new File(localFilePath);
        if (localFile.exists() && localFile.isFile()) {
            flag = uploadingSingle(localFile);
        }
        // 返回值
        return flag;
    }

    private static boolean uploadingSingle(File localFile) throws IOException {
        boolean flag;
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 上传单个文件
        flag = mClient.storeFile(localFile.getName(), inputStream);
        // 关闭文件流
        inputStream.close();
        return flag;
    }
    public boolean downloadFile(String localPath,FTPFile ftpFile) throws IOException {
        boolean result = false;
        //在本地创建对应文件夹目录
        File localFile= new File(localPath);
        if (!localFile.exists()) {
            localFile.createNewFile();
        }

                result = downloadSingle(localFile, ftpFile);


        return result;
    }

    /**
     * 下载单个文件,此时ftpFile必须在ftp工作目录下
     *
     * @param localFile 本地目录
     * @param ftpFile   FTP文件
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    public static boolean downloadSingle(File localFile, FTPFile ftpFile) throws IOException {
        boolean flag;
        // 创建输出流
        File file=new File(localFile,ftpFile.getName());
        if(!file.exists()){
            file.createNewFile();
        }
        OutputStream outputStream = new FileOutputStream(file);
        // 下载单个文件
        flag = mClient.retrieveFile(ftpFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;
    }

}

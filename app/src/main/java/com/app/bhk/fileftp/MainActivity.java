package com.app.bhk.fileftp;

import android.Manifest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private FtpFileAdapter adapter1;
    private List<File> FileList;
    private ProgressDialog progressDialog;
    private DrawerLayout mDrawerLayout;
    private ConnectBroadcastReceiver connectBroadcastReceiver;
    private FTPClient mClient;
    private  List<FTPFile> Ftplist=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        InitData();
        InitUI();
        InitMenu();
        InitBroadCast();
    }
    private void InitMenu(){
        NavigationView navigationView=(NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_setting:
                        Intent intent=new Intent(MainActivity.this,FtpConnectActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_FtpServerDirectory:
                        if(mClient!=null&&mClient.isConnected()){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FTPFile files[] = mClient.listFiles();

                                        for (int i = 0; i < files.length; i++) {
                                            boolean check = false;
                                            for (FTPFile ftpFile : Ftplist) {
                                                if (ftpFile.getName().equals(files[i].getName()))
                                                    check = true;
                                            }
                                            if (!check)
                                                Ftplist.add(files[i]);
                                        }
                                        mDrawerLayout.closeDrawer(GravityCompat.START);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                recyclerView.setAdapter(adapter1);
                                                adapter1.notifyDataSetChanged();
                                            }
                                        });


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                        else{
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(Ftplist.isEmpty())
                                        Toast.makeText(MainActivity.this,"还未连接服务器，请连接服务器后再查看！",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        break;
                    case R.id.nav_LocalDirectory:
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_close:
                        new Thread(new Runnable(){
                            public void run(){
                                try {
                                    if(FtpUtil.getmClient()!=null){
                                    FtpUtil.getmClient().disconnect();
                                    Ftplist.clear();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter1.notifyDataSetChanged();
                                        }
                                    });

                                    }
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"已经断开连接",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();


                }
                return true;
            }
        });
    }
    public  Object getAdapter(int selection){
        //selection=1返回adapter1 selection=0返回adapter

        return null;
    }
    private  void InitBroadCast(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.app.bhk.connected");
        connectBroadcastReceiver=new ConnectBroadcastReceiver();
        registerReceiver(connectBroadcastReceiver,intentFilter);
    }
    private void InitUI(){
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("正在复制...");

        recyclerView=(RecyclerView)findViewById(R.id.file_list);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new FileAdapter(FileList);
        adapter1=new FtpFileAdapter(Ftplist,this);
        recyclerView.setAdapter(adapter);
    }
    private void getPermissions(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(this,"没有权限打开相册",Toast.LENGTH_SHORT).show();
                }
            case 2:
        }
    }

    private void InitData(){
        File file =Environment.getExternalStorageDirectory();
        FileList=new ArrayList<File>( Arrays.asList(file.listFiles()));

    }
    @Override
    public void onBackPressed(){
        if(recyclerView.getAdapter()==adapter)
        {
            adapter.LastFile();
        }
        else{
            adapter1.LastFile();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.copy:
                if(FileUtil.getSaveFile()!=null) {
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(recyclerView.getAdapter()==adapter) {
                                FileUtil.Copy(FileUtil.getSaveFile(), adapter.getCurrentFile());
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       progressDialog.dismiss();
                                       FileList.add(FileUtil.getSaveFile());
                                       adapter.notifyDataSetChanged();
                                   }
                               });

                            }else{
                                try {
                                    FtpUtil.uploadFile(FileUtil.getSaveFile().getPath());
                                    Ftplist.clear();
                                    Ftplist.addAll( Arrays.asList(FtpUtil.getmClient().listFiles()));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();

                                            adapter1.notifyDataSetChanged();
                                        }
                                    });
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    }
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.copy_from_server:

                if(recyclerView.getAdapter()==adapter&&FtpUtil.getFtpFile()!=null) {
                    progressDialog.show();
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(recyclerView.getAdapter()==adapter){
                                        boolean result= FtpUtil.downloadSingle(adapter.getCurrentFile(), FtpUtil.getFtpFile());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                            }
                                        });

                                        if(result) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    FileList.clear();
                                                    FileList.addAll(Arrays.asList(adapter.getCurrentFile().listFiles()));
                                                    adapter.notifyDataSetChanged();

                                                }
                                            });
                                        }else{
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this,"复制失败",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }else{

                                    }


                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                }
                break;
        }
        return true;
    }
    class ConnectBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction().toString()){
                case "com.app.bhk.connected":
                    Toast.makeText(MainActivity.this,"连接上了",Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                                mClient=FtpUtil.getmClient();
                                adapter1.setFtpClient(mClient);


                        }
                    }).start();

                    break;
            }
        }
    }
}

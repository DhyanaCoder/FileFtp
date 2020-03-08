package com.app.bhk.fileftp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;


public class FtpConnectActivity extends AppCompatActivity {
    private FTPClient mFtpClient;
    private Button Connect_Button;
    private String address,password,user;
    private int port=0;
    private EditText addressInput,portInput,userInput,passwordInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_connect);

        SharedPreferences pref =getSharedPreferences("connectData",MODE_PRIVATE);
        address=pref.getString("address","X");
        password=pref.getString("password","X");
        user=pref.getString("user","X");
        port=pref.getInt("port",0);


        addressInput=(EditText)findViewById(R.id.address);
        portInput=(EditText)findViewById(R.id.port);
        userInput=(EditText)findViewById(R.id.user);
        passwordInput=(EditText)findViewById(R.id.password);

        if(address!=null&&!address.equals("X")){
            addressInput.setText(address);
        }
        if(password!=null&&!password.equals("X")){
            passwordInput.setText(password);
        }
        if(user!=null&&!user.equals("X")){
            userInput.setText(user);
        }
        if(port!=0){
            portInput.setText(" "+port);
        }

        Connect_Button=(Button)findViewById(R.id.connect_button);
        Connect_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address=addressInput.getText().toString();
                try {
                    port = Integer.parseInt(portInput.getText().toString().trim());

                }catch (NumberFormatException e){
                    port=0;
                    e.printStackTrace();
                }
                user=userInput.getText().toString();
                password=passwordInput.getText().toString();

                //信息保存
                SharedPreferences.Editor editor=getSharedPreferences("connectData",MODE_PRIVATE).edit();
                editor.putString("address",address);
                editor.putString("user",user);
                editor.putString("password",password);
                editor.putInt("port",port);
                editor.apply();

                attemptLogin();

            }
        });


    }
    private  void attemptLogin(){
        if(address.equals("")||port==0||user.equals("")||password.equals("")){
            Toast.makeText(this,"请填写完整的信息",Toast.LENGTH_SHORT).show();
            return;
        }else{
            FtpLogin(address,port,user,password);
        }
    }

    private  void FtpLogin(final String address, final int port, final String user, final String password ){
       FtpUtil.Init(address,port,user,password);
       if(FtpUtil.Connect()){
           //备注：后续，这里可以试着模仿okhttp的回调模式
           //如果连接成功
           Intent intent=new Intent();
           intent.setAction("com.app.bhk.connected");
           sendBroadcast(intent);
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(FtpConnectActivity.this, "success", Toast.LENGTH_SHORT).show();
               }
           });
       }else {
           //连接失败
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(FtpConnectActivity.this, "failure", Toast.LENGTH_SHORT).show();
               }
           });

       }

      /*  new Thread(new Runnable() {

           @Override
           public void run() {
                try {


                    if (!mFtpClient.isConnected()) {
                        mFtpClient.connect(address,port);
                        boolean status = mFtpClient.login(user, password);
                        if (status)
                            Log.d("test121", "212");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FtpConnectActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }
                    });
                FileUtil.setFtpClient(mFtpClient);
                Intent intent=new Intent();
                intent.setAction("com.app.bhk.connected");
                sendBroadcast(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FtpConnectActivity.this, "failure", Toast.LENGTH_SHORT).show();
                        }
                    });


                }

            }

        }).start();
        */
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();

    }
}

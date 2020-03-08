package com.app.bhk.fileftp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.app.bhk.fileftp.MyApplication.getContext;

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.ViewHolder> {
    private List<FTPFile>  FtpFilelist;
    private StringBuffer CurrentFile=new StringBuffer();
    private FTPClient ftpClient;
    private MainActivity mainActivity;

    public void LastFile(){//切换到父目录
      new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   int i= CurrentFile.toString().lastIndexOf('/');
                   if(i>=0) {
                       CurrentFile.delete(i, CurrentFile.length());
                       boolean f=FtpUtil.getmClient().changeToParentDirectory();
                       Log.d("test1234"," "+f+" "+CurrentFile);
                   }
                   FTPFile files[]=FtpUtil.getmClient().listFiles();
                   FtpFilelist.clear();
                   FtpFilelist.addAll(Arrays.asList(files));
                   mainActivity.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           notifyDataSetChanged();
                           return;
                       }
                   });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
       private ImageView File_imageview;
       private TextView File_textView;


        public ViewHolder(View v){
           super(v);
           File_imageview=(ImageView) v.findViewById(R.id.file_image);
           File_textView=(TextView) v.findViewById(R.id.file_name);

        }

    }
    public FtpFileAdapter(List<FTPFile> list,MainActivity activity){
           FtpFilelist=list;
           this.mainActivity=activity;

    }
    @Override
    public int getItemCount() {
        return FtpFilelist.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item1,parent,false);
        final ViewHolder viewHolder=new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               final FTPFile file=FtpFilelist.get(viewHolder.getAdapterPosition());
               if(file.isDirectory()) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    CurrentFile.append("/"+file.getName());
                                    boolean f= FtpUtil.getmClient().changeWorkingDirectory(CurrentFile.toString());

                                    FTPFile files[] = FtpUtil.getmClient().listFiles();
                                    FtpFilelist.clear();
                                    FtpFilelist.addAll(Arrays.asList(files));
                                    Log.d("test123",  "11212"+f+" "+CurrentFile);
                                       mainActivity.runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               notifyDataSetChanged();
                                               return;
                                           }
                                       });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();


                        Log.d("ftpfile1","good");
                      //  Toast.makeText(getContext(),"hello",Toast.LENGTH_SHORT).show();


                }

            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                //创建弹出式菜单对象（最低版本11）
                PopupMenu popup = new PopupMenu(getContext(), v);//第二个参数是绑定的那个view
                //获取菜单填充器
                final MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.operation_ftp, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FTPFile file=FtpFilelist.get(viewHolder.getAdapterPosition());
                        switch (item.getItemId()){
                            case R.id.copy_operate:
                                FtpUtil.setFtpFile(file);
                                break;

                            case R.id.delete_operate:
                                FtpFilelist.remove(file);
                                FtpUtil.deleteFtpFile(file);
                        }
                        notifyDataSetChanged();
                        return true;
                    }
                });

                popup.show();
                //显示(这一行代码不要忘记了)
                return true;
            }
        });




        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
     FTPFile ftpFile=FtpFilelist.get(position);

     holder.File_textView.setText(ftpFile.getName());
     if(ftpFile.isDirectory()){
         holder.File_imageview.setImageDrawable(getContext().getDrawable(R.drawable.directory));
     }else{
         holder.File_imageview.setImageDrawable(getContext().getDrawable(R.drawable.file));
     }
    }


}

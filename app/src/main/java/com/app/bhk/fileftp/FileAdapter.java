package com.app.bhk.fileftp;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.app.bhk.fileftp.MyApplication.getContext;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private List<File> arraylist;

    public File getCurrentFile() {
        return CurrentFile;
    }

    private File CurrentFile=Environment.getExternalStorageDirectory();//当前的文件夹
    static class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public ViewHolder(View v){
            super(v);
            imageView=(ImageView)v.findViewById(R.id.file_image);
            textView=(TextView)v.findViewById(R.id.file_name);

        }
    }
    public FileAdapter(List<File> arraylist){

        this.arraylist=arraylist;
    }
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item,parent,false);

         final ViewHolder holder =new ViewHolder(view);


        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File file=arraylist.get(holder.getAdapterPosition());
                if(file.isDirectory()) {
                    CurrentFile=file;
                    arraylist.clear();
                    arraylist.addAll(Arrays.asList(file.listFiles()));
                    notifyDataSetChanged();
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
                inflater.inflate(R.menu.operation, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        File file=arraylist.get(holder.getAdapterPosition());
                        switch (item.getItemId()){
                            case R.id.copy_operate:
                                FileUtil.setSaveFile(file);
                                break;
                            case R.id.delete_operate:
                                arraylist.remove(file);
                                file.delete();
                                break;
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
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
       File file=arraylist.get(position);
       holder.textView.setText(file.getName());
       if(file.isFile()){
         holder.imageView.setImageDrawable(getContext().getDrawable(R.drawable.file));
       }else{
           holder.imageView.setImageDrawable(getContext().getDrawable(R.drawable.directory));
       }
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }
    public void LastFile(){
        if (!CurrentFile.equals(Environment.getExternalStorageDirectory())){
            CurrentFile=CurrentFile.getParentFile();
            arraylist.clear();
            arraylist.addAll(Arrays.asList(CurrentFile.listFiles()));
            notifyDataSetChanged();
        }
    }
}

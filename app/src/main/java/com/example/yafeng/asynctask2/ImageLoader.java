package com.example.yafeng.asynctask2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Yafeng on 2015/7/8.
 */
public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;

    private LruCache<String,Bitmap> lruCache;
    private ListView mListView;
    private Set<MyAsyncTask> mTask;



    public ImageLoader(ListView listView){
        mListView = listView;
        mTask = new HashSet<>();
        // 获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory/4;
        lruCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    // 增加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if (getBitmapFromUrl(url) == null){
            lruCache.put(url,bitmap);
        }
    }

    // 从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return lruCache.get(url);
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mUrl))
                mImageView.setImageBitmap((Bitmap) msg.obj);
        }
    };

    public void showImageByThread(ImageView imageView, final String url){

        mImageView = imageView;
        mUrl = url;

        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromUrl(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }

    public Bitmap getBitmapFromUrl(String urlString){
        Bitmap bitmap;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            bitmap = BitmapFactory.decodeStream(inputStream);
            httpURLConnection.disconnect();
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void showImageByAsyncTask(ImageView imageView,String url){
        // 从缓存取出对应图片
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null)
           imageView.setImageResource(R.mipmap.ic_launcher);
        else
            imageView.setImageBitmap(bitmap);
    }

    // 加载可视范围内的图片
    public void loadImages(int start,int end){
        for (int i = start;i < end;i++){
            String url = NewsAdapter.URLS[i];

            Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap == null){
                MyAsyncTask task = new MyAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }
            else{
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTask() {
        if (mTask != null){
            for (MyAsyncTask task : mTask){
                task.cancel(false);
            }
        }
    }


    private class MyAsyncTask extends AsyncTask<String,Void,Bitmap>{

        //private ImageView mImageView;
        private String mUrl;

        public MyAsyncTask(String url){
            //mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            // 从网络获取图片
            Bitmap bitmap = getBitmapFromUrl(url);
            if (bitmap != null)
                // 将不在缓存的图片加入缓存
                addBitmapToCache(url,bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }

}

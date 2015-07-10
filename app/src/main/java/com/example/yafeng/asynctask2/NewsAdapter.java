package com.example.yafeng.asynctask2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Yafeng on 2015/7/8.
 */
public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private List<NewsBean> newsBeanList;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;

    private int mStart,mEnd;
    public static String[] URLS;

    private boolean mFirstIn;

    public NewsAdapter(Context context, List<NewsBean> data,ListView listView){
        newsBeanList = data;
        inflater = LayoutInflater.from(context);
        imageLoader = new ImageLoader(listView);
        URLS = new String[data.size()];
        for (int i = 0;i < data.size();i++){
            URLS[i] = data.get(i).newsIconUrl;
        }
        listView.setOnScrollListener(this);
        mFirstIn = true;
    }

    @Override
    public int getCount() {
        return newsBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_layout,null);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.news_item_image);
            viewHolder.title = (TextView) convertView.findViewById(R.id.news_item_title);
            viewHolder.article = (TextView) convertView.findViewById(R.id.news_item_article);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        String url = newsBeanList.get(position).newsIconUrl;
        viewHolder.icon.setImageResource(R.mipmap.ic_launcher);

        viewHolder.icon.setTag(url);

        //new imageLoader().showImageByThread(viewHolder.icon,url);
        imageLoader.showImageByAsyncTask(viewHolder.icon,url);


        viewHolder.title.setText(newsBeanList.get(position).newsTitleUrl);
        viewHolder.article.setText(newsBeanList.get(position).newsArticleUrl);

        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE){
            // 加载可见项目
            imageLoader.loadImages(mStart,mEnd);
        } else {
            imageLoader.cancelAllTask();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        if (mFirstIn == true && visibleItemCount > 0){
            imageLoader.loadImages(mStart,mEnd);
            mFirstIn = false;
        }
    }

    class ViewHolder{
        public TextView title,article;
        public ImageView icon;
    }
}

package net.swimmi.timelimit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swimmi on 2018/3/30.
 */

public class AppListAdapter extends BaseAdapter {

    private Context mContext;
    List<AppInfo> mList = new ArrayList<>();

    public AppListAdapter(List<AppInfo> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mViewHolder;
        AppInfo myAppInfo = (AppInfo) getItem(position);
        if (convertView == null) {
            mViewHolder = new AppListAdapter.ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_app_list, null);
            mViewHolder.iv_app_icon = convertView.findViewById(R.id.iv_app_icon);
            mViewHolder.tv_app_name = convertView.findViewById(R.id.tv_app_name);
            mViewHolder.tv_app_time = convertView.findViewById(R.id.tv_app_time);
            mViewHolder.iv_app_limit = convertView.findViewById(R.id.iv_app_limit);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (AppListAdapter.ViewHolder) convertView.getTag();
            mViewHolder.iv_app_limit.setTag(myAppInfo);
        }
        mViewHolder.iv_app_icon.setImageDrawable(myAppInfo.getIcon());
        mViewHolder.tv_app_name.setText(myAppInfo.getAppName());
        mViewHolder.iv_app_limit.setTag(myAppInfo);
        if(myAppInfo.isLimited())
            mViewHolder.iv_app_limit.setImageResource(R.drawable.ic_list_in);
        else
            mViewHolder.iv_app_limit.setImageResource(R.drawable.ic_list_add);
        mViewHolder.iv_app_limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppInfo appInfo = (AppInfo) mViewHolder.iv_app_limit.getTag();
                if(appInfo.isLimited()) {
                    mViewHolder.iv_app_limit.setImageResource(R.drawable.ic_list_add);
                    MainActivity.isLimitedSP.edit().remove(appInfo.getPackageName()).commit();
                } else {
                    mViewHolder.iv_app_limit.setImageResource(R.drawable.ic_list_in);
                    MainActivity.isLimitedSP.edit().putString(appInfo.getPackageName(), "").commit();
                }
                appInfo.setLimited(!appInfo.isLimited());
            }
        });
        return convertView;
    }

    class ViewHolder {

        ImageView iv_app_icon;
        TextView tv_app_name;
        TextView tv_app_time;
        ImageView iv_app_limit;
    }
}

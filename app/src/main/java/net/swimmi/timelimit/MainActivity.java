package net.swimmi.timelimit;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static String TAG = "MainActivity";
    private SwipeRefreshLayout mainSrl;
    private ListView appLv;
    private FloatingActionButton addFab;
    private RelativeLayout emptyRl;
    private AppAdapter mAdapter;
    private List<AppInfo> appInfoList;
    public Handler mHandler = new Handler();
    public static SharedPreferences isLimitedSP;
    private SharedPreferences isOpenSP;
    private IMyBinder iMyBinder;
    private List<String> packageList = new ArrayList<>();
    private MyServiceConnection connection;
    private boolean isRefresh = false;//是否刷新中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mainSrl = findViewById(R.id.srl_main);
        appLv = findViewById(R.id.lv_app);
        addFab = findViewById(R.id.fab_add);
        emptyRl = findViewById(R.id.rl_empty);
        //设置下拉刷新的监听
        mainSrl.setOnRefreshListener(this);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AppListActivity.class));
            }
        });

        isOpenSP = getSharedPreferences("isOpen", Context.MODE_PRIVATE);
        isLimitedSP = getSharedPreferences("isLimited", Context.MODE_PRIVATE);

        mAdapter = new AppAdapter();
        appLv.setAdapter(mAdapter);
        appLv.setEmptyView(emptyRl);
        initAppList();

        connection = new MyServiceConnection();
        bindService(new Intent(this, WatchDogService.class), connection, Context.BIND_AUTO_CREATE);

    }

    private void renderView() {
        initAppList();
        //Toast.makeText(getApplicationContext(), isOpenSP.getBoolean(packageList.get(0), false) ? packageList.get(0) + "已打开" : packageList.get(0) + "未打开", Toast.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void initAppList(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //扫描得到APP列表
                packageList.clear();
                packageList.addAll(isLimitedSP.getAll().keySet());
                appInfoList = AppTool.getAppStatus(getApplicationContext(), packageList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setData(appInfoList);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onRefresh() {
        //检查是否处于刷新状态
        if (!isRefresh) {
            isRefresh = true;
            new Handler().postDelayed(new Runnable() {
                public void run() {

                    //显示或隐藏刷新进度条
                    mainSrl.setRefreshing(false);
                    //修改adapter的数据
                    initAppList();
                    mAdapter.notifyDataSetChanged();
                    isRefresh = false;
                }
            }, 1000);
        }
    }

    class AppAdapter extends BaseAdapter {

        List<AppInfo> myAppList = new ArrayList<>();

        public void setData(List<AppInfo> myAppList) {
            this.myAppList = myAppList;
            notifyDataSetChanged();
        }

        public List<AppInfo> getData() {
            return myAppList;
        }

        @Override
        public int getCount() {
            if (myAppList != null && myAppList.size() > 0) {
                return myAppList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (myAppList != null && myAppList.size() > 0) {
                return myAppList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder;
            AppInfo myAppInfo = myAppList.get(position);
            if (convertView == null) {
                mViewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_app_limit, null);
                mViewHolder.iv_app_icon = convertView.findViewById(R.id.iv_app_icon);
                mViewHolder.tv_app_name = convertView.findViewById(R.id.tv_app_name);
                mViewHolder.tv_app_time = convertView.findViewById(R.id.tv_app_time);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            mViewHolder.iv_app_icon.setImageDrawable(myAppInfo.getIcon());
            mViewHolder.tv_app_name.setText(myAppInfo.getAppName());
            mViewHolder.tv_app_time.setText(myAppInfo.getTimeStr());
            return convertView;
        }

        class ViewHolder {

            ImageView iv_app_icon;
            TextView tv_app_name;
            TextView tv_app_time;
        }
    }

    public class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iMyBinder = (IMyBinder) iBinder;
            iMyBinder.setPackageNames(packageList);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }
}

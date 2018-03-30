package net.swimmi.timelimit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends AppCompatActivity {

    private static String TAG = "AppListActivity";
    private ListView appLv;
    private AppListAdapter mAdapter;
    private List<AppInfo> appInfoList;
    public Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_app_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        init();
    }

    private void init() {

        appLv = findViewById(R.id.lv_app);
        initAppList();
    }

    private void initAppList(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //扫描得到APP列表
                appInfoList = AppTool.getAppList(getApplicationContext().getPackageManager());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new AppListAdapter(appInfoList, getApplicationContext());
                        appLv.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }
}

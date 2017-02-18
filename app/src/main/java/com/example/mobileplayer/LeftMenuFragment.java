package com.example.mobileplayer;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by lenovo on 2017/2/3.
 */

public class LeftMenuFragment extends BaseFragment implements AdapterView.OnItemClickListener{
    private ListView lv_leftmenu;
    @Override
    public View initView() {
        lv_leftmenu = new ListView(mContext);
        //listview分隔线
        //lv_leftmenu.setDividerHeight(0);
        lv_leftmenu.setPadding(0,DensityUtil.dip2px(mContext,40),0,0);
        //lv_leftmenu.setSelector(android.R.color.transparent);
        lv_leftmenu.setOnItemClickListener(this);
        return lv_leftmenu;
    }

    @Override
    public void initData() {
        super.initData();
        String[] title = new String[]{"Setting","Exit"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,title);
        lv_leftmenu.setAdapter(arrayAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                break;
            case 1:
                MyActivityManager.getInstance().exit();
                break;
        }
    }
}

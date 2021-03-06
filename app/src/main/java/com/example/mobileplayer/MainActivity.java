package com.example.mobileplayer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;


/**
 * Created by lenovo on 2017/1/20.
 */

public class MainActivity extends SlidingFragmentActivity implements View.OnClickListener{

    public static final String LEFTMENU_TAG = "leftmenu_tag";
    private RadioButton rb_main_video;
    private RadioButton rb_main_audio,rb_main_netvideo;
    private RadioGroup rg_main_tag;
    private FrameLayout fl_main_taglist;
    private VideoFragments videoFragments;
    private AudioFragments audioFragments;
    private SlidingMenu slidingMenu;
    private NetVideoFreagment netVideoFreagment;
    private ImageButton iv_mainmenu;
    private android.app.Fragment currentFragment = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBehindContentView(R.layout.leftmenu);
        findView();
        setLeftMenu();
        initFragment();
    }

    private void initFragment() {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_leftmenu,new LeftMenuFragment(), LEFTMENU_TAG);
        transaction.commit();
    }

    private void setLeftMenu() {
        slidingMenu = this.getSlidingMenu();
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffset(DensityUtil.dip2px(this,150));
        slidingMenu.setFadeDegree(0.35f);
    }

    private void findView() {
        MyActivityManager.getInstance().addActivity(this);
        rb_main_video = (RadioButton) findViewById(R.id.rb_main_video);
        rb_main_audio = (RadioButton) findViewById(R.id.rb_main_audio);
        iv_mainmenu = (ImageButton) findViewById(R.id.iv_mainmenu);
        rb_main_netvideo = (RadioButton) findViewById(R.id.rb_main_netvideo);
        rg_main_tag = (RadioGroup) findViewById(R.id.rg_main_tag);
        fl_main_taglist = (FrameLayout) findViewById(R.id.fl_main_taglist);
        rb_main_video.setOnClickListener(this);
        rb_main_audio.setOnClickListener(this);
        rb_main_netvideo.setOnClickListener(this);
        iv_mainmenu.setOnClickListener(this);
        rg_main_tag.check(R.id.rb_main_video);
        videoFragments = new VideoFragments(this);
        audioFragments = new AudioFragments(this);
        netVideoFreagment = new NetVideoFreagment(this);
        setFragments(0);
    }

    /**fragment屏蔽**/
    private void setFragments(int fid){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(fid==0){
            //fragmentTransaction.replace(R.id.fl_main_taglist, videoFragments, null).commit();
            if(!videoFragments.isAdded()){
                fragmentTransaction.add(R.id.fl_main_taglist,videoFragments,"videoFragment").commit();
            }else{
                fragmentTransaction.hide(currentFragment).show(videoFragments).commit();
            }
            currentFragment = videoFragments;
        }else if(fid==1){
            //fragmentTransaction.replace(R.id.fl_main_taglist,audioFragments,null).commit();
            if(!audioFragments.isAdded()){
                fragmentTransaction.add(R.id.fl_main_taglist,audioFragments,"audioFragments").commit();
            }else{
                fragmentTransaction.hide(currentFragment).show(audioFragments).commit();
            }
            currentFragment = audioFragments;
        }else if(fid==2){
            //fragmentTransaction.replace(R.id.fl_main_taglist,netVideoFreagment,null).commit();
            if(!netVideoFreagment.isAdded()){
                fragmentTransaction.add(R.id.fl_main_taglist,netVideoFreagment,"netVideoFreagment").commit();
            }else{
                fragmentTransaction.hide(currentFragment).show(netVideoFreagment).commit();
            }
            currentFragment = netVideoFreagment;
        }

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rb_main_video:
                setFragments(0);
                break;
            case R.id.rb_main_audio:
                setFragments(1);
                break;
            case R.id.rb_main_netvideo:
                setFragments(2);
                break;
            case R.id.iv_mainmenu:
                slidingMenu.showMenu();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU){
            slidingMenu.showMenu();
        }
        return super.onKeyDown(keyCode, event);
    }

}

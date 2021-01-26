package com.huimv.szmc.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.huimv.android.basic.util.CommonUtil;
import com.huimv.szmc.R;
import com.huimv.szmc.app.HaifmPApplication;
import com.huimv.szmc.base.HaifmPBaseActivity;
import com.huimv.szmc.fragment.MenuLeftFragment;
import com.huimv.szmc.fragment.MenuRightFragment;
import com.huimv.szmc.fragment.MianWebviewFragment;
import com.huimv.szmc.fragment.WebViewFragment;
import com.huimv.szmc.messageEvent.MessageEvent;
import com.huimv.szmc.util.SharePreferenceUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends HaifmPBaseActivity implements OnClickListener, EasyPermissions.PermissionCallbacks {
    private ImageView account_icon;
    private ImageView message_icon;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private SharePreferenceUtil mSpUitl;
    String param;
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HaifmPApplication.addActivity(this);
        initRightMenu();// 初始化SlideMenu
        mSpUitl = HaifmPApplication.getInstance().getSpUtil();
        account_icon = (ImageView) findViewById(R.id.account_icon);
        message_icon = (ImageView) findViewById(R.id.message_icon);
        account_icon.setOnClickListener(this);
        message_icon.setOnClickListener(this);
        initSound();
        initUHF();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }
    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mFragments.clear();
        if (CommonUtil.isEmpty(param)) {
            MianWebviewFragment mMianWebviewFragment = new MianWebviewFragment();
            mFragments.add(mMianWebviewFragment);
        } else {
            WebViewFragment webViewFragment = WebViewFragment.newInstance(param);
            mFragments.add(webViewFragment);
        }
        /**             * 初始化Adapter
         */
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }
        };
        mViewPager.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_icon:
                //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                showRightMenu(v);
                break;
            case R.id.message_icon:
                showLeftMenu(v);
                break;
            default:
                break;
        }
    }

    public void showLeftMenu(View view) {
        //getSlidingMenu().showMenu();
    }

    public void showRightMenu(View view) {
        getSlidingMenu().showSecondaryMenu();
    }

    private void initRightMenu() {
        Fragment leftMenuFragment = new MenuLeftFragment();
        setBehindContentView(R.layout.left_menu_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
        SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.RIGHT);//设置只能右划
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        //menu.setBehindWidth()
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        // menu.setBehindScrollScale(1.0f);
        menu.setSecondaryShadowDrawable(R.drawable.shadow);
        //设置右边（二级）侧滑菜单
        menu.setSecondaryMenu(R.layout.right_menu_frame);
        Fragment rightMenuFragment = new MenuRightFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initViewPager();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("onKeyDown", "onKeyDown: ");
        if (keyCode == 139 ||keyCode == 280) {
            if (event.getRepeatCount() == 0) {
                readTag();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 139 ||event.getKeyCode() == 280) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getRepeatCount() == 0) {
                    readTag();
                }
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getRepeatCount() == 0) {
                    readTag();
                }
            }
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(MessageEvent event) {
        if (event.getId() == 1) {
            if (getSlidingMenu().isSecondaryMenuShowing()) {
                getSlidingMenu().toggle();
            }
        }
        if (event.getId() == 2) {
            if (getSlidingMenu().isSecondaryMenuShowing()) {
                getSlidingMenu().toggle();
            }
        }
    };


}

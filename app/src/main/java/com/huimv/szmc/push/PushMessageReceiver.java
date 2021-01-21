package com.huimv.szmc.push;

import java.util.ArrayList;

import org.json.JSONObject;

import com.huimv.android.basic.util.AndroidUtil;
import com.huimv.szmc.activity.MainActivity;
import com.huimv.szmc.app.HaifmPApplication;
import com.huimv.szmc.util.SharePreferenceUtil;
import com.huimv.szmc.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


public class PushMessageReceiver extends BroadcastReceiver {
    private SharePreferenceUtil mSpUtil = HaifmPApplication.getInstance()
            .getSpUtil();
    public static final int NOTIFY_ID = 0x000;
    private String bt;
    private String xxlx;
    private String xxid;
    public static ArrayList<EventHandler> ehList = new ArrayList<EventHandler>();

    public static abstract interface EventHandler {
        public abstract void onMessage(String message);

        public abstract void onNetChange(boolean isNetConnected);
    }

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView ==
     * null)
     */
    public static StringBuilder payloadData = new StringBuilder();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {// 网络变化
            boolean isNetConnected = AndroidUtil.isConn(context);
            for (int i = 0; i < ehList.size(); i++) {
                (ehList.get(i)).onNetChange(isNetConnected);
            }
        }
    }

    private void dataParse(String data) {
        try {
            JSONObject jo = new JSONObject(data);
            xxid = jo.get("id").toString();
            bt = (String) jo.get("bt");
            xxlx = (String) jo.get("xxlx");
        } catch (Exception e) {
        }
    }

    /**
     * 通知栏通知
     *
     * @param context
     * @param bt
     * @param xxid
     */
    private void onMessage(Context context, String bt, String xxid) {
        if (ehList.size() > 0) {// 有监听的时候，传递下去
            for (int i = 0; i < ehList.size(); i++)
                (ehList.get(i)).onMessage(bt);
        } else {
            showNotify(context, bt, xxid);
        }
    }

    @SuppressWarnings("deprecation")
    private void showNotify(Context context, String message, String xxid) {
        // 更新通知栏
        HaifmPApplication application = HaifmPApplication.getInstance();
        CharSequence tickerText = message;
        Intent intent = new Intent(application, MainActivity.class);
        intent.putExtra("xxid", xxid);
        PendingIntent contentIntent = PendingIntent.getActivity(application, 0,
                intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                HaifmPApplication.getInstance())
                .setTicker(tickerText)
                .setContentTitle("你有新消息")
                .setContentText(tickerText)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.haifm_icon);
        if (HaifmPApplication.isTimeOut) {
            builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setSound(Uri.parse("android.resource://"
                    + context.getPackageName() + "/" + R.raw.water_drop));
            HaifmPApplication.timeCount = 0;
            HaifmPApplication.isTimeOut = false;
        }
        Notification notification = builder.build();
        NotificationManagerCompat.from(HaifmPApplication.getInstance()).notify(NOTIFY_ID, notification);

/*		Notification notification = new Notification.Builder(HaifmPApplication.getInstance())
				.setContentTitle("你有新消息")
				.setContentText(tickerText)
				.setSmallIcon(R.drawable.haifm_icon.png)
				.setContentIntent(contentIntent)
				.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		// 设置默认声音
		// notification.defaults |= Notification.DEFAULT_SOUND;
		if (HaifmPApplication.isTimeOut) {
			notification.sound = Uri.parse("android.resource://"
					+ context.getPackageName() + "/" + R.raw.water_drop);
			// 设定震动(需加VIBRATE权限)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.contentView = null;
			HaifmPApplication.timeCount = 0;
			HaifmPApplication.isTimeOut = false;
		}
		application.getNotificationManager().notify(NOTIFY_ID, notification);// 通知一下才会生效哦*/
    }
}

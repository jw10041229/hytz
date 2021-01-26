package com.huimv.szmc.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.widget.ImageView;

import com.huimv.szmc.BuildConfig;
import com.huimv.szmc.R;
import com.huimv.szmc.thread.RecvThread;
import com.huimv.szmc.util.SharePreferenceUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tencent.bugly.Bugly;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class HaifmPApplication extends Application {
	SharePreferenceUtil mSpUtil;
	private static HaifmPApplication instance;
	private static final String SP_FILE_NAME = "haifmPlus_message";
	private static LinkedList<Activity> activityList = new LinkedList<>(); // 使用集合类统一管理Activity实例
	public static ImageLoader imageLoader = ImageLoader.getInstance();
	private static DisplayImageOptions options;
	private NotificationManager mNotificationManager;
	public String loadVersionName;
	private BluetoothAdapter m_BluetoothAdapter = null;
	private MessageDigest m = null;
	private final int MESSAGEREVICE = 0x01;
	public static int timeCount = 0;//计时次数
	public static boolean isTimeOut = true;//是否三秒内已经有声音或者震动
	public static int messCount;// 接受包的总数,超过30个作超时处理
	public static byte[] tempData;// 前一个包的遗留数据
	public static boolean isOpen = false;// 是否打开
	/** recv Thread **/
	private static RecvThread recvThread ;
	private String scjVersion = "";
	public String getScjVersion() {
		return scjVersion;
	}
	public void setScjVersion(String scjVersion) {
		this.scjVersion = scjVersion;
	}
	@Override
	public void onCreate() {
		upgrade();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.enableLogging().build();
		ImageLoader.getInstance().init(config);
		
		mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		//mSpUtil.setUniqueID(getUniqueID());// 得到手机唯一标识符
		
		instance = this;
		
		isTimeOut = true;
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = MESSAGEREVICE;
				handler.sendMessage(message);
			}
		};
		timer.schedule(task,0,1000);

		initBugly();
		super.onCreate();
	}
	/**
	 * 初始化bugly
	 */
	private void initBugly() {
		//CrashReport.initCrashReport(application, "", true);
		Bugly.init(this, "627bf454d1", BuildConfig.DEBUG);
	}
	private final Timer timer = new Timer();
	private TimerTask task;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MESSAGEREVICE) {
				timeCount++;
				if (timeCount == 3) {
					timeCount = 0;
					isTimeOut = true;
				}
			}
			super.handleMessage(msg);
		}
	};

	public synchronized static HaifmPApplication getInstance() {
		return instance;
	}

	public synchronized SharePreferenceUtil getSpUtil() {
		if (mSpUtil == null) {
			mSpUtil = new SharePreferenceUtil(this, SP_FILE_NAME);
		}
		return mSpUtil;
	}

	/** 添加Activity到容器，在每个Activity的onCreate调用 */
	public static void addActivity(Activity activity) {
		activityList.add(activity);
	}

	/** 遍历Activity并finish，在mainActivity的主界面当点击back键调用 */
	public static void exit() {
		// 遍历所有Activity实例，挨个finish
		for (Activity activity : activityList) {
			if (activity != null)
				activity.finish();
		}
		android.os.Process.killProcess(android.os.Process.myPid());// 获取当前进程PID，并杀掉
	}

	/**
	 * 图片异步加载
	 * 
	 * @param imageView
	 * @param url
	 */
	public static void initImageLoad(ImageView imageView, String url) {
		options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_empty).cacheInMemory()
				.cacheOnDisc().displayer(new RoundedBitmapDisplayer(5)).build();
		ImageLoader.getInstance().displayImage(url, imageView, options);
	}

	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}

	/** 得到APP版本号 */
	private void upgrade() {
		PackageManager nPackageManager = getPackageManager();// 得到包管理器
		try {
			PackageInfo nPackageInfo = nPackageManager.getPackageInfo(getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			loadVersionName = nPackageInfo.versionName;
			setLoadVersionName(loadVersionName);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 得到手机唯一标识符
	 * 
	 * @return
	 */
	private String getUniqueID() {
		String m_szUniqueID;// 手机唯一标识符
		// The IMEI: 仅仅只对Android手机有效:
		TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String szImei = TelephonyMgr.getDeviceId();
		// Pseudo-Unique ID, 这个在任何Android手机中都有效
		String m_szDevIDShort = "35" + // we make this look like a valid IMEI
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10
				+ Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
				+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
				+ Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
				+ Build.USER.length() % 10; // 13 digits
		// The Android ID 通常被认为不可信，因为它有时为null
		String m_szAndroidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		// The WLAN MAC Address string
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		// The BT MAC Address string
		m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		String m_szBTMAC = m_BluetoothAdapter.getAddress();
		// 上面五种拼成一个唯一标识符
		String m_szLongID = szImei + m_szDevIDShort + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
		// get md5 bytes
		byte p_md5Data[] = m.digest();
		// create a hex string
		m_szUniqueID = new String();
		for (int i = 0; i < p_md5Data.length; i++) {
			int b = (0xFF & p_md5Data[i]);
			if (b <= 0xF)
				m_szUniqueID += "0";
			m_szUniqueID += Integer.toHexString(b);
		} // hex string to uppercase
		m_szUniqueID = m_szUniqueID.toUpperCase();// 所有字符转化为大写
		return m_szUniqueID; 
	}

	public String getLoadVersionName() {
		return loadVersionName;
	}

	public void setLoadVersionName(String loadVersionName) {
		this.loadVersionName = loadVersionName;
	}

}

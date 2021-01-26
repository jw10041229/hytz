package com.huimv.szmc.base;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.huimv.szmc.R;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.rscja.deviceapi.RFIDWithUHF;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

/**
 * BaseActivity
 *
 * @author jiangwei
 */
public class HaifmPBaseActivity extends SlidingFragmentActivity {
    public RFIDWithUHF mReader;
    Button BtInventory;
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    Handler handler;
    public void initUHF() {
        try {
            mReader = RFIDWithUHF.getInstance();
        } catch (Exception ex) {
            Toast.makeText(HaifmPBaseActivity.this, ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mReader != null) {
            new InitTask().execute();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BtInventory = (Button)findViewById(R.id.BtInventory);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                //addEPCToList(strs[0], strs[1]);
                EventBus.getDefault().post(strs[0]);
                playSound(1);
            }
        };
    }

    /**
     * �豸�ϵ��첽��
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mReader.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(HaifmPBaseActivity.this, "读写设备初始化失败,请重启",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HaifmPBaseActivity.this, "读写设备初始化成功",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(HaifmPBaseActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("正在初始化设备...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }

    @Override
    protected void onDestroy() {

        if (mReader != null) {
            mReader.free();
        }
        super.onDestroy();
    }

    public void readTag() {
        if (BtInventory.getText().equals(getString(R.string.btInventory)))// 识别标签
        {
            switch (inventoryFlag) {
                case 0:// 单步
                {
                    String strUII = mReader.inventorySingleTag();
                    if (!TextUtils.isEmpty(strUII)) {
                        String strEPC = mReader.convertUiiToEPC(strUII);

                        //addEPCToList(strEPC, "N/A");
                        EventBus.getDefault().post(strEPC);
                        playSound(1);
                    } else {
                        //UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
                        playSound(2);
                    }
                }
                break;
                case 1:// 单标签循环
                {
                    if (mReader.startInventoryTag(0, 0)) {
                        BtInventory.setText(getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        new TagThread().start();
                    } else {
                        mReader.stopInventory();
                    }
                }
                break;
            }
        } else {// 停止识别
            stopInventory();
        }
    }
    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            if (mReader.stopInventory()) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                Toast.makeText(HaifmPBaseActivity.this,  R.string.uhf_msg_inventory_stop_fail,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }
    class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            String[] res = null;
            while (loopFlag) {
                res = mReader.readTagFromBuffer();
                Log.i("loopFlag", "loopFlag");
                if (res != null) {
                    strTid = res[0];
                    if (strTid.length() != 0 && !strTid.equals("0000000" +
                            "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    Log.i("data","EPC:"+res[1]+"|"+strResult);
                    Message msg = handler.obtainMessage();
                    //msg.obj = strResult + "EPC:" + mReader.convertUiiToEPC(res[1]) + "@" + res[2];
                    msg.obj  = mReader.convertUiiToEPC(res[1]);
                    handler.sendMessage(msg);
                }
            }
        }
    }

    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;

    public void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
    }
    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public void playSound(int id) {
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

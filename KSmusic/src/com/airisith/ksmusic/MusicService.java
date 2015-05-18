package com.airisith.ksmusic;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.airisith.lyric.LrcContent;
import com.airisith.lyric.LrcProcess;
import com.airisith.util.Constans;

@SuppressLint("NewApi")
public class MusicService extends Service {
	private static String TAG = "MusicService";
	
	private static MediaPlayer mediaPlayer = new MediaPlayer(); // 媒体播放器对象
	private String path; // 音乐文件路径
	private boolean isPause; // 暂停状态
	private static String frontActivity = Constans.ACTIVITY_HOME; //当前activity，用于结束广播发送给哪个Activity

	private LrcProcess mLrcProcess; //歌词处理  
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象  
	private int index = 0;          //歌词检索值 
	private Handler handler; // 传递歌词显示的handler
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int cmd = intent.getIntExtra("CMD",0);
		frontActivity = intent.getStringExtra("Activity");
		if (cmd != Constans.ACTIVITY_CHANGED_CMD) {
			path = intent.getStringExtra("url");
			int rate = intent.getIntExtra("rate", 0);
			Log.w(TAG, "CMD:"+cmd+",rate"+rate);
			if (cmd == Constans.PLAY_CMD) {
				if (rate >= 0) {
					play(rate);
				} else {
					mediaPlayer.start();
				}
			} else if (cmd == Constans.PUASE_CMD) {
				pause();
			} else if (cmd == Constans.STOP_CMD) {
				stop();
			}
			
		} else {
			//do nothing,只是为了通知service， Activity改变了
			Log.w(TAG, "页面切换到："+frontActivity);
		}
		if (frontActivity.equals(Constans.ACTIVITY_MUSIC)) {
			try {
				initLrc();
			} catch (Exception e) {
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 播放音乐
	 * 
	 * @param position
	 */
	private void play(int position) {
		try {
			Log.w(TAG, "play");
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare(); // 进行缓冲
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));// 注册一个player准备好监听器
			mediaPlayer.setOnCompletionListener(new MusicCompleteListener()); // 注册一个播放结束的监听器
			TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务  
	        telManager.listen(new MobliePhoneStateListener(),  
	                PhoneStateListener.LISTEN_CALL_STATE); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPause = true;
		}
	}

	/**
	 * 停止音乐
	 */
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
				mediaPlayer.release();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
	 * 
	 */
	private final class PreparedListener implements OnPreparedListener {
		// 播放的位置，0-100
		private int position;
		public PreparedListener(int positon) {
			this.position = positon;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start(); // 开始播放
			if (position > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(position * (mediaPlayer.getDuration()/100));
			}
		}
	}

	/**
	 * 使用定时器，timer.schedule(TimerTask task, long delayTime, long period)
	 * 每一秒获取一次歌曲时间，发送给Activity,mediaPlayer返回时间是毫秒
	 * 
	 * @param handler
	 *            由Activity传进来的handler，用于将数据发送出去
	 * @param timer
	 *            由Activity创建的Timer
	 * @param startTimer
	 *            是否启动，否则关闭定时器
	 */
	public static void updataTime(final Handler handler, Timer timer,
			Boolean startTimer) {
		if (startTimer) {
			timer.schedule(new TimerTask() {
				public void run() {
					Message message = handler.obtainMessage();
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					int progress = current*200/total;
					String[] time = formatTime(current, total);
					message.obj = time;
					message.what = progress;
					handler.sendMessage(message);
				}
			}, 1000, 1000);
		} else {
			try {
				timer.cancel();
			} catch (Exception e) {
			}
		}

	}

	/**
	 * 将拿到的时间转换成00:00-00:00的形式用于显示
	 * 
	 * @param time
	 * @return
	 */
	private static String[] formatTime(int currentTime, int totalTime) {
		String current_mStr, current_sStr, total_mStr, total_sStr;
		int current_m = (currentTime / 1000) / 60;
		int current_s = (currentTime / 1000) % 60;
		int total_m = (totalTime / 1000) / 60;
		int total_s = (totalTime / 1000) % 60;
		String[] timeStr = new String[2];
		if (current_m < 10) {
			current_mStr = "0" + current_m;
		} else {
			current_mStr = "" + current_m;
		}
		if (current_s < 10) {
			current_sStr = "0" + current_s;
		} else {
			current_sStr = "" + current_s;
		}
		if (total_m < 10) {
			total_mStr = "0" + total_m;
		} else {
			total_mStr = "" + total_m;
		}
		if (total_s < 10) {
			total_sStr = "0" + total_s;
		} else {
			total_sStr = "" + total_s;
		}
		timeStr[0] = current_mStr + ":" + current_sStr;
		timeStr[1] = total_mStr + ":" + total_sStr;
		return timeStr;
	}

	/**
	 * 音乐播放结束，发送广播给Activity
	 * 
	 * @author Administrator
	 * 
	 */
	private class MusicCompleteListener implements OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			Intent intent = new Intent();
			if (frontActivity.equals(Constans.ACTIVITY_HOME)) {
				intent.setAction(Constans.MUSIC_END_ACTION_HOME);
			} else if(frontActivity.equals(Constans.ACTIVITY_MUSIC )){
				intent.setAction(Constans.MUSIC_END_ACTION_MUSIC);
			} else {
				intent.setAction(Constans.MUSIC_END_ACTION_HOME);
			}
			sendBroadcast(intent);
		}
	}
	
	/**
     * 初始化歌词配置
     */
   public void initLrc(){  
       mLrcProcess = new LrcProcess();  
       //读取歌词文件  
       mLrcProcess.readLRC(path);
       Log.w(TAG, "path");
       //传回处理后的歌词文件  
       lrcList = mLrcProcess.getLrcList();  
       MusicView.lrcView.setmLrcList(lrcList);  
       //切换带动画显示歌词  
       MusicView.lrcView.setAnimation(AnimationUtils.loadAnimation(MusicService.this,R.anim.anim)); 
       // 1.生成Message对象，将Runnable对象赋值给callback属性；2.调用sendMessageDelay（Message msg）将消息发送出去
       handler.post(mRunnable);  
   }  
   /**
    * 更新歌词
    */
   Runnable mRunnable = new Runnable() {  
         

	@Override  
       public void run() {  
    	   MusicView.lrcView.setIndex(lrcIndex(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition()));  
    	   MusicView.lrcView.invalidate();  
           handler.postDelayed(mRunnable, 100);  
       }  
   }; 


   /** 
    * 根据时间获取歌词显示的索引值 
    * @return 
    */  
   public int lrcIndex(int duration , int currentTime) {  
       if(currentTime < duration) {  
           for (int i = 0; i < lrcList.size(); i++) {  
               if (i < lrcList.size() - 1) {  
                   if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {  
                       index = i;  
                   }  
                   if (currentTime > lrcList.get(i).getLrcTime()  
                           && currentTime < lrcList.get(i + 1).getLrcTime()) {  
                       index = i;  
                   }  
               }  
               if (i == lrcList.size() - 1  
                       && currentTime > lrcList.get(i).getLrcTime()) {  
                   index = i;  
               }  
           }  
       }  
       return index;  
   }  
   
   /**
	 * 电话监听器类
	 * @author 
	 * 
	 */
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
				if (isPause) {
					mediaPlayer.start();
					isPause = false;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:	//通话状态
			case TelephonyManager.CALL_STATE_RINGING:	//响铃状态
				if(mediaPlayer.isPlaying()){
					pause();
				}
				break;
			default:
				break;
			}
		}
	}
}

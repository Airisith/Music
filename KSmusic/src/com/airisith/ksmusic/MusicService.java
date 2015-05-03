package com.airisith.ksmusic;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.airisith.util.Constans;

@SuppressLint("NewApi")
public class MusicService extends Service {
	private static MediaPlayer mediaPlayer = new MediaPlayer(); // 媒体播放器对象
	private String path; // 音乐文件路径
	@SuppressWarnings("unused")
	private boolean isPause; // 暂停状态
	private String TAG = "MusicService";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		path = intent.getStringExtra("url");
		int cmd = intent.getIntExtra("CMD",0);
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
			mediaPlayer.stop();
			mediaPlayer.release();
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
					String time = formatTime(current, total);
					message.obj = time;
					handler.sendMessage(message);
				}
			}, 1000, 1000);
		} else {
			timer.cancel();
		}

	}

	/**
	 * 将拿到的时间转换成00:00-00:00的形式用于显示
	 * 
	 * @param time
	 * @return
	 */
	private static String formatTime(int currentTime, int totalTime) {
		String current_mStr, current_sStr, total_mStr, total_sStr;
		int current_m = (currentTime / 1000) / 60;
		int current_s = (currentTime / 1000) % 60;
		int total_m = (totalTime / 1000) / 60;
		int total_s = (totalTime / 1000) % 60;
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

		return current_mStr + ":" + current_sStr + "-" + total_mStr + ":"
				+ total_sStr;
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
			intent.setAction(Constans.MUSIC_END_ACTION);
			sendBroadcast(intent);
		}
	}
}

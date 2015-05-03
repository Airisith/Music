package com.airisith.ksmusic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.airisith.modle.MusicInfo;
import com.airisith.util.Constans;
import com.airisith.util.MusicList;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.drm.DrmStore.Playback;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

@SuppressWarnings("unused")
public class HomeActivity extends Activity implements OnTabChangeListener {

	private static final String TAG = "HomeActivity";

	private final String TAB_ID_MINE = "mine";
	private final String TAB_ID_LIB = "musicLib";
	private TabHost tabHost;
	private ListView localListView;
	private ImageView bcap;
	private TextView bTitle;
	private TextView bTime;
	private TextView bArtis;
	private ImageView bPlay;
	private ImageView bNext;
	private ImageView bOrder;
	private RelativeLayout bInfoLayout;

	private Handler timeHandler; // 实时更新歌曲时间
	private Timer timer;
	private int localMusicPosition = 0; // 记录歌曲位置
	private Intent musicIntent; // 启动service的intent
	private List<MusicInfo> localMusicLists; // 本地音乐列表
	private int playModle = Constans.MODLE_ORDER; // 播放模式
	private int playState = Constans.STATE_STOP;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		localListView = (ListView) findViewById(R.id.home_localListView);
		bcap = (ImageView) findViewById(R.id.homeb_cap);
		bTitle = (TextView) findViewById(R.id.homeb_title);
		bTime = (TextView) findViewById(R.id.homeb_time);
		bArtis = (TextView) findViewById(R.id.homeb_artist);
		bPlay = (ImageView) findViewById(R.id.homeb_play);
		bNext = (ImageView) findViewById(R.id.homeb_next);
		bOrder = (ImageView) findViewById(R.id.homeb_order);
		bInfoLayout = (RelativeLayout) findViewById(R.id.homeb_infoLayout);

		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();

		TabHost.TabSpec specMine = tabHost.newTabSpec("TAB_ID_MINE");
		specMine.setContent(R.id.home_tabFirst);
		specMine.setIndicator("我的", null);
		tabHost.addTab(specMine);
		TabHost.TabSpec specLib = tabHost.newTabSpec("TAB_ID_LIB");
		specLib.setContent(R.id.home_tabSecond);
		specLib.setIndicator("音乐库", null);
		tabHost.addTab(specLib);

		// 设置标签背景颜色
		tabHost.getTabWidget().setStripEnabled(false);
		tabHost.getTabWidget().getChildAt(0)
				.setBackgroundColor(Color.alpha(100));
		tabHost.getTabWidget().getChildAt(1)
				.setBackgroundColor(Color.alpha(100));
		updateTab(tabHost); // 初始化标签字体颜色
		tabHost.setOnTabChangedListener(this); // 选择监听器

		// 加载本地音乐库
		localMusicLists = MusicList.getMusicInfos(getApplicationContext());
		MusicList.setListAdpter(getApplicationContext(), localMusicLists,
				localListView);
		localListView.setOnItemClickListener(new MusicListItemClickListener(
				localMusicLists));

		// 创建Intent对象，准备启动MusicService
		musicIntent = new Intent(getApplicationContext(), MusicService.class);

		// 广播接收器，用于一首歌播放完成后继续播放下一首的动作
		MusicCompleteReceiver receiver = new MusicCompleteReceiver();
		IntentFilter intentfFilter = new IntentFilter();
		intentfFilter.addAction(Constans.MUSIC_END_ACTION);
		HomeActivity.this.registerReceiver(receiver, intentfFilter);

		// 给底部按钮注册监听器
		bInfoLayout.setOnClickListener(new OnButtomMenuClickedListener());
		bPlay.setOnClickListener(new OnButtomMenuClickedListener());
		bNext.setOnClickListener(new OnButtomMenuClickedListener());
		bOrder.setOnClickListener(new OnButtomMenuClickedListener());

		// 更新时间，接收由MusicService中的子线程发送的消息
		timer = new Timer(true);
		timeHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String time = msg.obj.toString();
				bTime.setText(time);
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/**
	 * 更新Tab标签的颜色，和字体的颜色
	 * 
	 * @param tabHost
	 */
	@SuppressLint("InlinedApi")
	private void updateTab(TabHost tabHost) {
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			View view = tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(android.R.id.title);
			tv.setTextSize(20);
			tv.setTypeface(Typeface.SERIF, 2); // 设置字体和风格
			if (tabHost.getCurrentTab() == i) {// 选中
				tv.setTextColor(this.getResources().getColorStateList(
						android.R.color.white));
			} else {// 不选中
				tv.setTextColor(this.getResources().getColorStateList(
						android.R.color.holo_blue_bright));
			}
		}
	}

	/**
	 * 设置TabHost监听器
	 * 
	 * @author Administrator
	 * 
	 */
	@Override
	public void onTabChanged(String tabId) {
		updateTab(tabHost);
		if (tabId.equals(TAB_ID_MINE)) {
			// 更新list
			List<MusicInfo> localLists = MusicList
					.getMusicInfos(getApplicationContext());
			MusicList.setListAdpter(getApplicationContext(), localLists,
					localListView);
			localListView
					.setOnItemClickListener(new MusicListItemClickListener(
							localLists));
			localMusicLists = localLists;
		}
	}

	/**
	 * 点击item监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class MusicListItemClickListener implements OnItemClickListener {
		private List<MusicInfo> musicInfos = null;

		public MusicListItemClickListener(List<MusicInfo> musicInfos) {
			this.musicInfos = musicInfos;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			localMusicPosition = position;
			MusicCommad(musicInfos, Constans.PLAY_CMD, position, 0, true);
		}
	}

	/**
	 * 歌曲命令
	 * 
	 * @param musicInfos
	 *            歌曲列表信息
	 * @param playCommand
	 *            播放命令：play，puase，stop
	 * @param position
	 *            歌曲位于列表中的位置
	 * @param rate
	 *            播放的位置，整个歌曲时间定为100, 如果为负数，则表示继续从当前位置播放
	 * @param upTime
	 *            是否更新时间
	 */
	private void MusicCommad(List<MusicInfo> musicInfos, int playCommand,
			int position, int rate, Boolean upTime) {
		if (musicInfos != null) {
			if (position <= musicInfos.size()) {
				MusicInfo musicInfo = musicInfos.get(position);
				Log.w(TAG, "点击了第" + position + "首歌");
				Log.w(TAG, musicInfo.getUrl().toString());
				// Intent intent = new Intent();
				musicIntent.putExtra("url", musicInfo.getUrl());
				musicIntent.putExtra("CMD", playCommand);
				musicIntent.putExtra("rate", rate);
				startService(musicIntent); // 启动服务
				bTitle.setText(musicInfo.getAbbrTitle());
				bArtis.setText(musicInfo.getArtist());
				bPlay.setImageResource(R.drawable.puase);
				bcap.setImageBitmap(musicInfo.getAlbum_bitmap());
				MusicService.updataTime(timeHandler, timer, upTime);
				playState = Constans.STATE_PLAY;
			} else {
				localMusicPosition = 0;
			}
		}
	}

	/**
	 * 音乐播放结束广播接收器，继续播放
	 * 
	 * @author Administrator
	 * 
	 */
	private class MusicCompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (playModle) {
			case Constans.MODLE_ORDER:
				localMusicPosition = localMusicPosition + 1;
				break;
			case Constans.MODLE_SINGLE:
				break;

			case Constans.MODLE_RANDOM:
				localMusicPosition = (int) (Math.random() * localMusicLists.size());
				break;

			default:
				break;
			}
			MusicCommad(localMusicLists, Constans.PLAY_CMD, localMusicPosition, 0,
					true);
		}
	}

	/**
	 * 底部按钮的监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class OnButtomMenuClickedListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.homeb_play:
				if (Constans.STATE_PLAY == playState) {
					MusicCommad(localMusicLists, Constans.PUASE_CMD,
							localMusicPosition, 0, true);
					bPlay.setImageResource(R.drawable.play);
					playState = Constans.STATE_PUASE;
				} else if (Constans.STATE_PUASE == playState) {
					MusicCommad(localMusicLists, Constans.PLAY_CMD,
							localMusicPosition, -1, true);
					bPlay.setImageResource(R.drawable.puase);
					playState = Constans.STATE_PLAY;
				} else {
					MusicCommad(localMusicLists, Constans.PLAY_CMD,
							localMusicPosition, 0, true);
					bPlay.setImageResource(R.drawable.puase);
					playState = Constans.STATE_PLAY;
				}
				break;
			case R.id.homeb_next:
				if (Constans.MODLE_RANDOM == playModle) {
					localMusicPosition = (int) (Math.random() * localMusicLists
							.size());
				} else {
					localMusicPosition = localMusicPosition + 1;
				}
				MusicCommad(localMusicLists, Constans.PLAY_CMD, localMusicPosition,
						0, true);

				break;
			case R.id.homeb_infoLayout:

				break;
			case R.id.homeb_order:
				if (Constans.MODLE_ORDER == playModle) {
					playModle = Constans.MODLE_RANDOM;
					bOrder.setImageResource(R.drawable.random);
				} else if (Constans.MODLE_RANDOM == playModle) {
					playModle = Constans.MODLE_SINGLE;
					bOrder.setImageResource(R.drawable.single);
				} else {
					playModle = Constans.MODLE_ORDER;
					bOrder.setImageResource(R.drawable.order);
				}

				break;
			default:
				break;
			}
		}
	}
}

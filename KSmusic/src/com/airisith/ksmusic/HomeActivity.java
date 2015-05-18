package com.airisith.ksmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.airisith.database.MusicListDatabase;
import com.airisith.modle.MusicInfo;
import com.airisith.util.BluToothConnect;
import com.airisith.util.Constans;
import com.airisith.util.MusicList;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class HomeActivity extends Activity implements OnTabChangeListener {

	private static final String TAG = "HomeActivity";

	// 返回键退出延时时间
	private long mExitTime;

	private final String TAB_ID_MINE = "mine";
	private final String TAB_ID_LIB = "musicLib";
	private TabHost tabHost;
	private ListView localListView;
	private ExpandableListView expandableListView;
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
	private Boolean turnTOback = true;
	private int musicPosition = 0; // 记录歌曲位置
	private int currentListId = 0; // 当前列表Id
	private Intent musicIntent; // 启动service的intent
	private List<String> groupArray = null; // 列表分组group
	private List<MusicInfo> localMusicLists = null; // 本地音乐列表
	private List<MusicInfo> userMusicLists = null; // 用户收藏列表
	private List<MusicInfo> downloadMusicLists = null; // 下载音乐列表
	private List<MusicInfo> currentMusicList = null; // 当前选择的音乐列表
	private HashMap<Integer, List<MusicInfo>> musicLists = null; // 将两个列表包装在map中
	private int playMode = Constans.MODLE_ORDER; // 播放模式
	private int playState = Constans.STATE_STOP;

	private MusicInfo currentMusicInfo = null;
	// 循环播放广播接收器
	private MusicCompleteReceiver receiver;

	@SuppressLint({ "HandlerLeak", "UseSparseArrays" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Log.w(TAG, "onCreate");
		expandableListView = (ExpandableListView) findViewById(R.id.home_ExpandingListView);
		bcap = (ImageView) findViewById(R.id.homeb_cap);
		bTitle = (TextView) findViewById(R.id.homeb_title);
		bTime = (TextView) findViewById(R.id.homeb_time);
		bArtis = (TextView) findViewById(R.id.homeb_artist);
		bPlay = (ImageView) findViewById(R.id.homeb_play);
		bNext = (ImageView) findViewById(R.id.homeb_next);
		bOrder = (ImageView) findViewById(R.id.homeb_order);
		bInfoLayout = (RelativeLayout) findViewById(R.id.homeb_infoLayout);

		try {
			playState = getIntent().getIntExtra("SERVICE_STATE",
					Constans.STATE_STOP);
		} catch (Exception e) {
		}
		// 先设为空对象
		userMusicLists = new ArrayList<MusicInfo>();
		downloadMusicLists = new ArrayList<MusicInfo>();

		// tab设置
		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		TabHost.TabSpec specMine = tabHost.newTabSpec(TAB_ID_MINE);
		specMine.setContent(R.id.home_tabFirst);
		specMine.setIndicator("我的", null);
		tabHost.addTab(specMine);
		TabHost.TabSpec specLib = tabHost.newTabSpec(TAB_ID_LIB);
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

		// 加载本地音乐库，默认列表为本地列表
		musicLists = new HashMap<Integer, List<MusicInfo>>();

		// 创建Intent对象，准备启动MusicService
		musicIntent = new Intent(getApplicationContext(), MusicService.class);
		musicIntent.putExtra("Activity", Constans.ACTIVITY_HOME);

		// 广播接收器，用于一首歌播放完成后继续播放下一首的动作
		receiver = new MusicCompleteReceiver();
		IntentFilter intentfFilter = new IntentFilter();
		intentfFilter.addAction(Constans.MUSIC_END_ACTION_HOME);
		HomeActivity.this.registerReceiver(receiver, intentfFilter);

		// 给底部按钮注册监听器
		bInfoLayout.setOnClickListener(new OnButtomMenuClickedListener());
		bPlay.setOnClickListener(new OnButtomMenuClickedListener());
		bNext.setOnClickListener(new OnButtomMenuClickedListener());
		bOrder.setOnClickListener(new OnButtomMenuClickedListener());

	}

	@Override
	protected void onDestroy() {
		Log.w(TAG, "onDestroy");
		try {
			MusicService.updataTime(timeHandler, timer, false);
			// 停止接收广播
			HomeActivity.this.unregisterReceiver(receiver);
			stopService(musicIntent);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.w(TAG, "onPause");
		turnTOback = true;
		super.onPause();
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onStart() {
		Log.w(TAG, "onStart");
		updataMusicList();
		super.onStart();
		turnTOback = false;
		// 通知service，页面发生改变
		musicIntent.putExtra("Activity", Constans.ACTIVITY_HOME);
		try {
			musicIntent.putExtra("CMD", Constans.ACTIVITY_CHANGED_CMD);
			startService(musicIntent);
		} catch (Exception e) {
		}
		try {
			// 获取歌曲播放信息
			int[] state = MusicInfo
					.getCurrentMusicInfo(getApplicationContext());
			if (0 == state[0]) {
				currentMusicList = localMusicLists;
				currentListId = 0;
			} else if (1 == state[0]) {
				currentMusicList = userMusicLists;
				currentListId = 1;
			} else if (2 == state[0]) {
				currentMusicList = downloadMusicLists;
				currentListId = 2;
			}
			playMode = state[1];
			musicPosition = state[2];
		} catch (Exception e) {
		}
		Log.w(TAG, "列表:" + currentMusicList.size() + "循环模式:" + playMode
				+ "歌曲位置:" + musicPosition);
		try {
			currentMusicInfo = currentMusicList.get(musicPosition);
			Log.w(TAG, "歌曲:" + currentMusicInfo.getAbbrTitle());
		} catch (Exception e) {
		}
		try {
			Log.w(TAG, "是否正在播放" + playState);
			// 更新时间，接收由MusicService中的子线程发送的消息
			timer = new Timer(true);
			timeHandler = new UpdateInfoHandler();
			bTitle.setText(currentMusicInfo.getAbbrTitle());
			bArtis.setText(currentMusicInfo.getArtist());
			if (Constans.STATE_STOP == playState) {
				bPlay.setImageResource(R.drawable.play);
				bTime.setText("00:00-" + currentMusicInfo.getDurationStr());
			} else if (Constans.STATE_PUASE == playState) {
				bPlay.setImageResource(R.drawable.play);
				MusicService.updataTime(timeHandler, timer, true);
			} else {
				MusicService.updataTime(timeHandler, timer, true);
				bPlay.setImageResource(R.drawable.puase);
				bcap.setImageBitmap(currentMusicInfo.getAlbum_bitmap());
				bTitle.setText(currentMusicInfo.getAbbrTitle());
				bArtis.setText(currentMusicInfo.getAbbrArtist());
			}

		} catch (Exception e) {
		}
		try {
			IntentFilter intentfFilter = new IntentFilter();
			intentfFilter.addAction(Constans.MUSIC_END_ACTION_HOME);
			HomeActivity.this.registerReceiver(receiver, intentfFilter);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onStop() {
		Log.w(TAG, "onStop");
		turnTOback = true;
		super.onStop();
		try {
			MusicService.updataTime(timeHandler, timer, false);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.w(TAG, "onNewIntent");
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
						android.R.color.holo_blue_bright));
			} else {// 不选中
				tv.setTextColor(this.getResources().getColorStateList(
						android.R.color.white));
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
//			// 更新本地list
//			List<MusicInfo> localLists = MusicList
//					.getLocaMusicInfos(getApplicationContext());
//			List<MusicInfo> userLists = MusicList
//					.getMusicsFromeProvider(getApplicationContext());
//			localMusicLists = localLists;
//			userMusicLists = userLists;
//		
//			MusicList.setListAdpter(getApplicationContext(),
//					expandableListView, musicLists, groupArray);
		}
	}

	/**
	 * expandableListView的子列表Item点击事件监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class MusicListItemClickListener implements OnChildClickListener {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			if (0 == groupPosition) {
				currentMusicList = localMusicLists;
				currentListId = 0;
				Log.w(TAG, "选中localMusicLists");
			} else if (1 == groupPosition) {
				currentMusicList = userMusicLists;
				currentListId = 1;
				Log.w(TAG, "选中userMusicLists");
			} else if (2 == groupPosition) {
				currentMusicList = downloadMusicLists;
				currentListId = 2;
				Log.w(TAG, "选中downloadMusicLists");
			}
			musicPosition = childPosition;
			MusicCommad(currentMusicList, Constans.PLAY_CMD, musicPosition, 0,
					true);
			return false;
		}
	}

	/**
	 * expandableListView的子列表Item长点击事件监听器,
	 * 前提是在adapter中分别对groupView和childView设置了tag
	 * 
	 * @author Administrator
	 * 
	 */
	private class onItemLongclichedListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			int groupPos = (Integer) arg1.getTag(R.layout.musiclist_group); // 参数值是在setTag时使用的对应资源id号
			int childPos = (Integer) arg1.getTag(R.layout.musiclist_item);
			if (childPos == -1) {// 长按的是父项
				// 根据groupPos判断长按的是哪个父项，做相应处理（弹框等）
			} else {
				// 根据groupPos及childPos判断长按的是哪个父项下的哪个子项，然后做相应处理。
				switch (groupPos) {
				case 0: //第一个列表，本地列表
					localListDialog(childPos);
					break;
				case 1: //第二个列表，收藏列表
					luserListDialog(childPos);
					break;
				case 2: //第三个列表，下载列表
					
					break;
					
					
				default:
					break;
				}
			}
			// 这里设置为true，否则长按也会触发点击事件
			return true;
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
		if ((musicInfos != null)
				&& (Constans.ACTIVITY_CHANGED_CMD != playCommand)) {
			MusicInfo musicInfo = musicInfos.get(position);
			currentMusicInfo = musicInfo;
			Log.w(TAG, "开始播放第" + position + "首歌");
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
			if (false == turnTOback) {
				MusicService.updataTime(timeHandler, timer, upTime);
			}

			MusicInfo.putCurrentMusicInfo(getApplicationContext(),
					currentListId, playMode, musicPosition);
			Log.w(TAG, "保存歌曲信息：list,mode,position:" + currentListId + playMode
					+ musicPosition);
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
			if (Constans.MODLE_ORDER == playMode) {
				if (musicPosition < currentMusicList.size() - 1) {
					musicPosition = musicPosition + 1;
				} else {
					musicPosition = 0;
				}
			} else if (Constans.MODLE_RANDOM == playMode) {
				musicPosition = (int) (Math.random() * currentMusicList.size());
			} else {

			}
			playState = Constans.STATE_PLAY;
			MusicCommad(currentMusicList, Constans.PLAY_CMD, musicPosition, 0,
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
					MusicCommad(currentMusicList, Constans.PUASE_CMD,
							musicPosition, 0, true);
					bPlay.setImageResource(R.drawable.play);
					playState = Constans.STATE_PUASE;
				} else if (Constans.STATE_PUASE == playState) {
					MusicCommad(currentMusicList, Constans.PLAY_CMD,
							musicPosition, -1, true);
					bPlay.setImageResource(R.drawable.puase);
					playState = Constans.STATE_PLAY;
				} else {
					MusicCommad(currentMusicList, Constans.PLAY_CMD,
							musicPosition, 0, true);
					bPlay.setImageResource(R.drawable.puase);
					playState = Constans.STATE_PLAY;
				}
				break;
			case R.id.homeb_next:
				if (Constans.MODLE_RANDOM == playMode) {
					musicPosition = (int) (Math.random() * currentMusicList
							.size());
				} else {
					if (musicPosition < currentMusicList.size() - 1) {
						musicPosition = musicPosition + 1;
					} else {
						musicPosition = 0;
					}
				}
				playState = Constans.STATE_PLAY;
				MusicCommad(currentMusicList, Constans.PLAY_CMD, musicPosition,
						0, true);

				break;
			case R.id.homeb_infoLayout:
				Intent intent = new Intent(getApplicationContext(),
						MusicView.class);
				intent.putExtra("SERVICE_STATE", playState);
				startActivity(intent);
				break;
			case R.id.homeb_order:
				if (Constans.MODLE_ORDER == playMode) {
					playMode = Constans.MODLE_RANDOM;
					bOrder.setImageResource(R.drawable.random);
				} else if (Constans.MODLE_RANDOM == playMode) {
					playMode = Constans.MODLE_SINGLE;
					bOrder.setImageResource(R.drawable.single);
				} else {
					playMode = Constans.MODLE_ORDER;
					bOrder.setImageResource(R.drawable.order);
				}

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 接收service发送的消息，实时更新时间
	 * 
	 * @author Administrator
	 * 
	 */
	@SuppressLint("HandlerLeak")
	private class UpdateInfoHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			try {
				String[] time = (String[]) msg.obj;
				bTime.setText(time[0] + "-" + time[1]);
			} catch (Exception e) {
			}
		}

	}

	/**
	 * 屏蔽返回键原来的功能，以免程序异常退出出错
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			Log.w(TAG, "KEYCODE_HOME");
			return true;
		case KeyEvent.KEYCODE_BACK:
			Log.w(TAG, "KEYCODE_BACK");
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Object mHelperUtils;
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();

			} else {
				try {
					MusicService.updataTime(timeHandler, timer, false);
					// 停止接收广播
					HomeActivity.this.unregisterReceiver(receiver);
					stopService(musicIntent);
				} catch (Exception e) {
				}
				// finish();
				System.exit(0); // 退出整个程序，否则service，MusicView未退出，下次打开再次启动service会出错
			}
			return true;
		case KeyEvent.KEYCODE_CALL:
			Log.w(TAG, "KEYCODE_CALL");
			return true;
		case KeyEvent.KEYCODE_SYM:
			Log.w(TAG, "KEYCODE_SYM");
			return true;
		case KeyEvent.KEYCODE_STAR:
			Log.w(TAG, "KEYCODE_STAR");
			return true;
		}
		Log.w(TAG, "return super.onKeyDown(keyCode, event);");
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 刷新歌曲列表
	 */
	private void updataMusicList(){
		localMusicLists = MusicList.getLocaMusicInfos(getApplicationContext());
		userMusicLists = MusicList
				.getMusicsFromeProvider(getApplicationContext());
		currentMusicList = localMusicLists;
		currentListId = 0;
		musicLists.put(0, localMusicLists);
		musicLists.put(1, userMusicLists);
		musicLists.put(2, downloadMusicLists);
		// expandableListView设置
		expandableListView.setGroupIndicator(null); // 设置 属性 GroupIndicator
													// 去掉默认向下的箭头
		expandableListView.setCacheColorHint(0); // 设置拖动列表的时候防止出现黑色背景
		groupArray = new ArrayList<String>();
		groupArray.add(">本地列表" + "(" + localMusicLists.size() + ")");
		groupArray.add(">我的收藏" + "(" + userMusicLists.size() + ")");
		groupArray.add(">下载歌曲" + "(" + downloadMusicLists.size() + ")");

		// 将音乐加载到列表,并设置监听器
		MusicList.setListAdpter(getApplicationContext(), expandableListView,
				musicLists, groupArray);
		expandableListView
				.setOnChildClickListener(new MusicListItemClickListener());
		expandableListView
				.setOnItemLongClickListener(new onItemLongclichedListener());
	}
	
	/**
	 * 本地列表歌曲长按弹出对话框
	 * @param position
	 */
	private void localListDialog(final int position){
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		builder.setPositiveButton("收藏", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MusicListDatabase.insertMusic(getApplicationContext(), localMusicLists.get(position));
				updataMusicList();
			}
		});
		builder.setNegativeButton("删除", new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.setNeutralButton("分享", new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String filePath = localMusicLists.get(position).getUrl();
				new BluToothConnect(getApplicationContext(), filePath).sendFile();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * 收藏列表弹出对话框
	 * @param position
	 */
	private void luserListDialog(final int position){
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		builder.setPositiveButton("移除", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MusicListDatabase.deleteMusic(getApplicationContext(), userMusicLists.get(position));
				updataMusicList();
			}
		});
		builder.setNegativeButton("分享", new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String filePath = userMusicLists.get(position).getUrl();
				new BluToothConnect(getApplicationContext(), filePath).sendFile();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}

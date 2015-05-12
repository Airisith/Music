package com.airisith.util;

public class Constans {
	//广播消息
	public final static String MUSIC_END_ACTION_HOME = "com.home.musicstoped";
	public final static String MUSIC_END_ACTION_MUSIC = "com.music.musicstoped";
	//Activity的名字
	public final static String ACTIVITY_HOME = "HomeActivity";
	public final static String ACTIVITY_MUSIC = "MusicView";
	
	//playCommand常量
	public final static int PLAY_CMD = 0;
	public final static int PUASE_CMD = 1;
	public final static int STOP_CMD = 2;
	public final static int MODLE_ORDER = 3;
	public final static int MODLE_RANDOM = 4;
	public final static int MODLE_SINGLE = 5;
	
	public final static int STATE_PLAY = 6;
	public final static int STATE_PUASE = 7;
	public final static int STATE_STOP = 8;
	
	public final static int ACTIVITY_CHANGED_CMD = 9;
	
	// 保存数据的名字
	public final static String PREFERENCES_NAME_MUSIC_STATE = "musicstate";
	// 定义两种类型：本地音乐类型，网络音乐类型
	public final static int TYPE_LOCAL = 100;
	public final static int TYPE_NET = 101;

}

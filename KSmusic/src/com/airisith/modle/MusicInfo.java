package com.airisith.modle;

import com.airisith.util.Constans;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;


public class MusicInfo {
	private long id = 0; //id
	private long album_id =0; //专辑ID
	private String title = null; //标题
	private String artist = null; // 歌手
	private long duration = 0; // 时长
	private long size = 0;
	private String url = null;
	private Bitmap album_bitmap = null; // 图片
	private int type = 0;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	// 获取歌名缩写
	public String getAbbrTitle()
	{
		String abbrTitle = null;
		if(title.length() > 20){
			abbrTitle = title.substring(0, 20) + "...";
		}
		else {
			abbrTitle = title;
		}
		return abbrTitle;
	}
	public String getArtist() {
		return artist;
	}
	public String getAbbrArtist(){
		String abbrArtist = null;
		if(artist.length() > 20){
			abbrArtist = artist.substring(0, 20) + "...";
		}
		else {
			abbrArtist = artist;
		}
		return abbrArtist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getDurationStr(){
		long m = (duration/1000)/60;
		long s = (duration/1000)%60;
		String mString, sString;
		if (m < 10) {
			mString = "0" + m;
		} else {
			mString = "" + m;
		}
		if (s < 10) {
			sString = "0" + s;
		} else {
			sString = "" + s;
		}
		return mString + ":" + sString;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getAlbum_id() {
		return album_id;
	}
	public void setAlbum_id(long album_id) {
		this.album_id = album_id;
	}
	public Bitmap getAlbum_bitmap() {
		return album_bitmap;
	}
	public void setAlbum_bitmap(Bitmap album_bitmap) {
		this.album_bitmap = album_bitmap;
	}

	/**
	 *  储存播放状态，ACtivity切换时或者下次打开时继续播放
	 * @param context
	 * @param current_MusicList
	 * @param current_mode
	 * @param current_positon
	 */
	public static void putCurrentMusicInfo(Context context, int current_MusicList, int current_mode, int current_positon){
		SharedPreferences sp = context.getSharedPreferences(Constans.PREFERENCES_NAME_MUSIC_STATE, Context.MODE_APPEND);
		//存入数据
		Editor editor = sp.edit();
		editor.putInt("MUSIC_LIST_ID", current_MusicList);
		editor.putInt("CURRENT_MODE", current_mode);
		editor.putInt("CURRENT_POSITION", current_positon);
	    editor.commit();
	}
	
	/**
	 * 获取播放状态
	 * @param context
	 * @return 歌曲列表：0为本地... , 循环模式，歌曲位置
	 */
	public static int[] getCurrentMusicInfo(Context context){
		int[] state = new int[3];
		SharedPreferences sp = context.getSharedPreferences(Constans.PREFERENCES_NAME_MUSIC_STATE, Context.MODE_APPEND);
		state[0] = sp.getInt("MUSIC_LIST_ID", 0);
		state[1] = sp.getInt("CURRENT_MODE", 3);
		state[2] = sp.getInt("CURRENT_POSITION", 0);
		return state;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}

package com.airisith.modle;

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
		 
}

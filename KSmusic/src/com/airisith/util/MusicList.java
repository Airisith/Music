package com.airisith.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.airisith.ksmusic.R;
import com.airisith.modle.MusicInfo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MusicList {

	private static String TAG = "MusicList";

	/**
	 * 用于从数据库中查询歌曲的信息，保存在List当中
	 * 
	 * @return
	 */
	public static List<MusicInfo> getMusicInfos(Context context) {
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null,null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		List<MusicInfo> musicInfosInfos = new ArrayList<MusicInfo>();
		try {
			for (int i = 0; i < cursor.getCount(); i++) {
				MusicInfo musicInfo = new MusicInfo();
				cursor.moveToNext();
				long id = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media._ID)); // 音乐id
				
				long album_id = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)); // 专辑ID 
				
				String title = cursor.getString((cursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE)));// 音乐标题
				String artist = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST));// 艺术家
				long duration = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.DURATION));// 时长
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
				int isMusic = cursor.getInt(cursor
						.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));// 是否为音乐
				if (isMusic != 0) { // 只把音乐添加到集合当中
					musicInfo.setId(id);
					musicInfo.setAlbum_id(album_id);
					musicInfo.setTitle(title);
					musicInfo.setArtist(artist);
					musicInfo.setDuration(duration);
					musicInfo.setSize(size);
					musicInfo.setUrl(url);
					// 获取专辑图片
					try {
						Bitmap album_bitmap = ArtworkUtils.getArtwork(context, title, 
								id, album_id, true);
						musicInfo.setAlbum_bitmap(album_bitmap);
					} catch (Exception e) {
						Log.e(TAG , e.toString());
					}
					
					musicInfosInfos.add(musicInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return musicInfosInfos;
	}

	/** 
     * 填充列表 
     * @param musicInfos 
     */  
    public static void setListAdpter(Context context, List<MusicInfo> musicInfos, ListView listView) {  
        List<HashMap<String, String>> musiclist = new ArrayList<HashMap<String, String>>();  
        for (Iterator<MusicInfo> iterator = musicInfos.iterator(); iterator.hasNext();) {  
            MusicInfo musicInfo = (MusicInfo) iterator.next();  
            HashMap<String, String> map = new HashMap<String, String>();  
            map.put("title", musicInfo.getAbbrTitle());  
            map.put("Artist", musicInfo.getArtist());  
            map.put("duration", (musicInfo.getDurationStr()));  
            map.put("size", String.valueOf(musicInfo.getSize()));  
            map.put("url", musicInfo.getUrl());  
            musiclist.add(map);  
        }  
        SimpleAdapter localListAdapter = new SimpleAdapter(context, musiclist,  
                R.layout.local_musiclist_item, new String[] { "title", "Artist", "duration" },  
                new int[] { R.id.localListTitel, R.id.localListArtist, R.id.localListTime});  
        listView.setAdapter(localListAdapter);
    }  
}
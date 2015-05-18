package com.airisith.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.airisith.database.MusicListDatabase;
import com.airisith.database.MusicProvider;
import com.airisith.modle.MusicInfo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ExpandableListView;

public class MusicList {

	private static String TAG = "MusicList";

	/**
	 * 用于从数据库中查询歌曲的信息，保存在List当中
	 * 
	 * @return
	 */
	public static List<MusicInfo> getLocaMusicInfos(Context context) {
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
					musicInfo.setType(Constans.TYPE_LOCAL);
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
		cursor.close();
		return musicInfosInfos;
	}

	/**
	 * 填充列表 
	 * @param context
	 * @param expandableListView
	 * @param musicLists 所有音乐列表
	 * @param groupLists 列表名称
	 */
    public static void setListAdpter(Context context,ExpandableListView expandableListView, 
    		HashMap<Integer, List<MusicInfo>> musicLists, List<String> groupLists) {
        ExpandableListAdapter listAdapter = new ExpandableListAdapter(context, groupLists, musicLists);
        expandableListView.setAdapter(listAdapter);
    }  
    
    /**
     * 向数据库中添加多首歌曲
     * @param context
     * @param musics
     */
    public static void addAllMusicsToDatabase(Context context, List<MusicInfo> musics){
    	Iterator<MusicInfo> iterator = musics.iterator();
    	while (iterator.hasNext()) {
			MusicInfo musicInfo = (MusicInfo) iterator.next();
			MusicListDatabase.insertMusic(context, musicInfo);
		}
    }
    
    /**
     * 将从contentProvider中获得的音乐空的成员填充
     * @param context
     * @return
     */
    public static List<MusicInfo> getMusicsFromeProvider(Context context){
    	List<MusicInfo> musics = MusicListDatabase.getMusics(context);
    	Iterator<MusicInfo> iterator = musics.iterator();
    	while (iterator.hasNext()) {
			MusicInfo musicInfo = (MusicInfo) iterator.next();
			String title = musicInfo.getTitle();
			// 将provider中的本地音乐的各项成员匹配到musicinfo
			if (Constans.TYPE_LOCAL == musicInfo.getType()) {
				Cursor cursor = context.getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
						MusicProvider.MusicColumns.TITLE + "=?", new String[] { title + "" },
						MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				if (cursor != null && cursor.moveToFirst()) {
					// 需要重新设置路径，因为如果路径变了，歌曲就会找不到
					String url = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.DATA));
					long id = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media._ID)); // 音乐id
					long album_id = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)); // 专辑ID 
					String artist = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.ARTIST));// 艺术家
					long duration = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media.DURATION));// 时长
					long size = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
					int isMusic = cursor.getInt(cursor
							.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));// 是否为音乐
					if (isMusic != 0) { // 只把音乐添加到集合当中
						musicInfo.setUrl(url);
						musicInfo.setId(id);
						musicInfo.setAlbum_id(album_id);
						musicInfo.setArtist(artist);
						musicInfo.setDuration(duration);
						musicInfo.setSize(size);
						// 获取专辑图片
						try {
							Bitmap album_bitmap = ArtworkUtils.getArtwork(context, title, 
									id, album_id, true);
							musicInfo.setAlbum_bitmap(album_bitmap);
						} catch (Exception e) {
							Log.e(TAG , e.toString());
						}
					}
				} else {
					Log.e(TAG, "query failure!");
				}	
				cursor.close();
			}
		}
    	
    	return musics;
    }
}

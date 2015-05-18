package com.airisith.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.airisith.modle.MusicInfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class MusicListDatabase {

	private static final String TAG = "MusicListDatabase";

	/**
	 * 向数据库中添加歌曲
	 * 
	 * @param context
	 * @param musicInfo
	 */
	public static int insertMusic(Context context, MusicInfo musicInfo) {
		Uri uri = null;
		
		String eds1 = musicInfo.getTitle().toString();
		String eds2 = musicInfo.getUrl().toString();
		String eds3 = String.valueOf(musicInfo.getType());
		ContentValues content = new ContentValues();
		try { //可能查询不到，所以try
			if (eds1.equals(queryMusic(context, musicInfo).getTitle())) {
				if (eds2.equals(queryMusic(context, musicInfo).getUrl())) {
					Toast.makeText(context, "已经添加过这首歌了", Toast.LENGTH_LONG).show();
					return 0;
				}
			} 
		} catch (Exception e) {
		}
		if (!eds1.equals("") && !eds2.equals("") && !eds3.equals("")) {
			content.put(MusicProvider.MusicColumns.TITLE, eds1);
			content.put(MusicProvider.MusicColumns.URL, eds2);
			content.put(MusicProvider.MusicColumns.TYPE, eds3);

			uri = context.getContentResolver().insert(
					MusicProvider.MusicColumns.CONTENT_URI, content);
			Toast.makeText(context, "添加成功", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "添加失败", Toast.LENGTH_LONG).show();
		}
		String lastPath = uri.getLastPathSegment();
    	if (TextUtils.isEmpty(lastPath)) {
    		Log.w(TAG, "insert failure!");
    	} else {
    		Log.w(TAG, "insert success! the id is " + lastPath);
    	}
		return Integer.parseInt(lastPath);
	}
	
	/**
	 * 查询数据库中的某首歌
	 * @param context
	 * @param musicInfo ：必须为包含title的歌曲
	 * @return
	 */
	public static MusicInfo queryMusic(Context context, MusicInfo musicInfo){
		MusicInfo music = null;
		String title = musicInfo.getTitle().toString();
		Cursor cursor = context.getContentResolver().query(MusicProvider.MusicColumns.CONTENT_URI, 
				new String[] { MusicProvider.MusicColumns.TITLE, MusicProvider.MusicColumns.URL,MusicProvider.MusicColumns.TYPE }, 
				MusicProvider.MusicColumns.TITLE + "=?", new String[] { title + "" }, null);
		// 添加元素
		if (cursor != null && cursor.moveToFirst()) {
			music = new MusicInfo();
			music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MusicProvider.MusicColumns.TITLE)));
			music.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(MusicProvider.MusicColumns.URL)));
			music.setType(cursor.getInt(cursor.getColumnIndexOrThrow(MusicProvider.MusicColumns.TYPE)));
		} else {
			Log.e(TAG, "query failure!");
		}		
		cursor.close();
		return music;
	}
	
	/**
	 * 删除数据库中的歌曲
	 * 
	 * @param context
	 * @param musicInfo
	 */
	public static void deleteMusic(Context context, MusicInfo musicInfo) {
		String title = musicInfo.getTitle().toString();
		String url = musicInfo.getUrl().toString();
		//String type = String.valueOf(musicInfo.getType());
		if (!title.equals("") || !url.equals("")) {
			HashMap<String, String[]> wheres = wheres(title, url, ""); //type为空，否则会将所有type匹配的都删除
			String sql = wheres.get("sql")[0];
			String[] selectags = wheres.get("selectages");
			context.getContentResolver().delete(
					MusicProvider.MusicColumns.CONTENT_URI, sql, selectags);
			Toast.makeText(context, "移除成功", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "移除失败", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 修改歌曲
	 * 
	 * @param context
	 * @param srcMusicInfo :新的歌曲
	 * @param destMusicInfo ：原来的歌曲
	 * @return ：修改的位置（数据库中的哪一行）
	 */
	public static int changeMusic(Context context, MusicInfo srcMusicInfo,
			MusicInfo destMusicInfo) {
		int number = 0;
		String srcEds1 = srcMusicInfo.getTitle().toString();
		String srcEds2 = srcMusicInfo.getUrl().toString();
		String srcEds3 = String.valueOf(srcMusicInfo.getType());
		String destEds1 = destMusicInfo.getTitle().toString();
		String destEds2 = destMusicInfo.getUrl().toString();
		String destEds3 = String.valueOf(destMusicInfo.getType());

		ContentValues values = new ContentValues();
		values.put(MusicProvider.MusicColumns.TITLE, srcEds1);
		values.put(MusicProvider.MusicColumns.URL, srcEds2);
		values.put(MusicProvider.MusicColumns.TYPE, srcEds3);
		if (!destEds1.equals("") || !destEds2.equals("")) {
			HashMap<String, String[]> wheres = wheres(destEds1, destEds2,
					destEds3);
			String sql = wheres.get("sql")[0];
			String[] selectags = wheres.get("selectages");
			number = context.getContentResolver().update(
					MusicProvider.MusicColumns.CONTENT_URI, values, sql,
					selectags);

		} else {
			Toast.makeText(context, "替换失败", Toast.LENGTH_LONG).show();
		}
		return number;
	}
	
	/**
	 * 获取数据库中的音乐列表
	 * 
	 * @param context
	 * @return :数据库中所有歌曲，但是每首歌只有title,url,type三项属性，其他属性为默认值
	 */
	public static List<MusicInfo> getMusics(Context context) {
		List<MusicInfo> list = new ArrayList<MusicInfo>();
		Log.w(TAG, "尝试获取数据库中的数据");
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(
				MusicProvider.MusicColumns.CONTENT_URI, null,
				null, null, null);
		cursor.getCount();
		while (cursor.moveToNext()) {
			MusicInfo music = new MusicInfo();
			music.setTitle(cursor.getString((cursor
					.getColumnIndex(MusicProvider.MusicColumns.TITLE))));
			music.setUrl(cursor.getString((cursor
					.getColumnIndex(MusicProvider.MusicColumns.URL))));
			int type = Integer.parseInt(cursor.getString((cursor
					.getColumnIndex(MusicProvider.MusicColumns.TYPE))));
			Log.w(TAG, "获取："+music.getAbbrTitle());
			music.setType(type);
			list.add(music);
			Log.w(TAG, music.getTitle()+":从本地数据库中获取歌曲列表");
		}
		cursor.close();

		return list;
	}
	
	/** 用来判别条件
	 * 
	 * @param eds1
	 *            :titel --歌曲名
	 * @param eds2
	 *            :url --歌曲url
	 * @param eds3
	 *            :type --歌曲类型
	 * @return
	 */
	public static HashMap<String, String[]> wheres(String eds1, String eds2,
			String eds3) {
		HashMap<String, String[]> where = new HashMap<String, String[]>();
		if (!eds1.equals("") && !eds2.equals("") && !eds3.equals("")) {
			// 设置查询语句格式
			String[] sql = { MusicProvider.MusicColumns.TITLE + "=? and "
					+ MusicProvider.MusicColumns.URL + " =? and"
					+ MusicProvider.MusicColumns.TYPE };
			String[] selectages = { eds1, eds2, eds3 };
			where.put("sql", sql);
			where.put("selectages", selectages);

		}
		// 添加筛选条件
		// type为空
		if (!eds1.equals("") || !eds2.equals("") && eds3.equals("")) {
			String[] sql = { MusicProvider.MusicColumns.TITLE + "=? " };
			String[] selectages = { eds1 };
			where.put("sql", sql);
			where.put("selectages", selectages);

		}
		// type和title都为空
		if (eds1.equals("") && !eds2.equals("") && eds3.equals("")) {
			String[] sql = { MusicProvider.MusicColumns.URL + " =?" };
			String[] selectages = { eds2 };
			where.put("sql", sql);
			where.put("selectages", selectages);

		}
		// title和uri为空
		if (eds1.equals("") && eds2.equals("") && !eds3.equals("")) {
			String[] sql = { MusicProvider.MusicColumns.TYPE + " =?" };
			String[] selectages = { eds3 };
			where.put("sql", sql);
			where.put("selectages", selectages);

		}
		return where;
	}
}

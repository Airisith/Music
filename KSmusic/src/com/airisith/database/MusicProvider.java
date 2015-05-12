package com.airisith.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 存放跟数据库有关的常量
 * @author 
 *
 */
public class MusicProvider {
	
	// 这个是每个Provider的标识，在Manifest中使用
	public static final String AUTHORITY = "com.airisith.provider.music";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.airisith.music";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.airisith.music";

    /**
     * 跟Person表相关的常量
     * @author 
     *
     */
	public static final class MusicColumns implements BaseColumns {
		// CONTENT_URI跟数据库的表关联，最后根据CONTENT_URI来查询对应的表
		public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/musics");
		public static final String TABLE_NAME = "music";
		public static final String DEFAULT_SORT_ORDER = "title desc";
		
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String TYPE = "type";
		
	}
	
}

package com.airisith.util;

import java.util.HashMap;
import java.util.List;

import com.airisith.ksmusic.R;
import com.airisith.modle.MusicInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

@SuppressLint("UseSparseArrays") 
public class ExpandableListAdapter extends BaseExpandableListAdapter{

	private List<String> groupArray; //列表分组group
	private HashMap<Integer, List<MusicInfo>> groupMusicLists; // 音乐列表,包含两个子项：本地列表和自定义列表
	private HashMap<Integer, HashMap<Integer, View>> rowViewsList = new HashMap<Integer, HashMap<Integer,View>>(); //View缓存(包含所有分组子项)
	private HashMap<Integer, View> rowViews = new HashMap<Integer, View>(); //View缓存(子项)
	private HashMap<Integer, View> groupViews = new HashMap<Integer, View>(); //View缓存（组项）
	private Context context = null;
	
	/**
	 * 
	 * @param context
	 * @param groupArray :分组名字
	 * @param groupMusicLists ：音乐列表组：每个组都包含一个子列表
	 */
	public ExpandableListAdapter(Context context, List<String> groupArray, HashMap<Integer, List<MusicInfo>> groupMusicLists){
		this.context = context;
		this.groupArray = groupArray;
		this.groupMusicLists = groupMusicLists;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return groupMusicLists.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, 
			boolean isLastChild, View convertView, ViewGroup parent) {
		View rowView = null;
		rowViews = rowViewsList.get(groupPosition);
		if (null == rowViews) {
			rowViews = new HashMap<Integer, View>();
		} else {
			rowView = rowViews.get(childPosition);
		}
		if(null == rowView){
			//生成一个LayoutInflater对象
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			//调用LayoutInflater对象的inflate方法，可以生成一个View对象
			rowView = layoutInflater.inflate(R.layout.musiclist_item, null);
			//得到该View当中的控件
			TextView titelView = (TextView)rowView.findViewById(R.id.localListTitel);
			TextView artistView = (TextView)rowView.findViewById(R.id.localListArtist);
			TextView timeView = (TextView)rowView.findViewById(R.id.localListTime);
			
			List<MusicInfo> musicInfos = groupMusicLists.get(groupPosition);
			if (false == musicInfos.isEmpty()) {
				MusicInfo musicInfo = musicInfos.get(childPosition);
				
				try {
					titelView.setText(musicInfo.getAbbrTitle());
					artistView.setText(musicInfo.getAbbrArtist());
					timeView.setText(musicInfo.getDurationStr());
					
				} catch (Exception e) {
				}
				// 给子项设置TAG，用于ExpandableListView长按事件判断是哪个item，这里第一个必须为该项的资源layout或id
				rowView.setTag(R.layout.musiclist_group, groupPosition);
				rowView.setTag(R.layout.musiclist_item, childPosition);
				// 添加到组项，再将组项添加到list
				rowViews.put(childPosition, rowView);
				rowViewsList.put(groupPosition, rowViews);
			}
		}
		
        return rowView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return groupMusicLists.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupMusicLists.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupMusicLists.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View groupview = groupViews.get(groupPosition);
		if (groupview == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			//调用LayoutInflater对象的inflate方法，可以生成一个View对象
			groupview = layoutInflater.inflate(R.layout.musiclist_group, null);
			//得到该View当中的控件
			TextView groupTextView = (TextView) groupview.findViewById(R.id.home_groups);
			String groupStr = groupArray.get(groupPosition);
			groupTextView.setText(groupStr);
			groupViews.put(groupPosition, groupview);
			//设置tag，用于ExpandableListView长按事件判断是哪个group，第二个设置为-1，判断是group
			groupview.setTag(R.layout.musiclist_group, groupPosition);
			groupview.setTag(R.layout.musiclist_item, -1);
		}
		return groupview;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}

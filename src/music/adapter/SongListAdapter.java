package music.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import music.activity.R;
import music.util.GetMedia;
import music.util.ListContent;

public class SongListAdapter extends ArrayAdapter<ListContent>{

	int resourceId;//歌曲列表的布局
	public SongListAdapter(Context context, int textViewResourceId, List<ListContent> objects) {
		super(context,textViewResourceId,objects);
		resourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListContent listContent = getItem(position);
		View view;
		ViewHolder viewHolder = new ViewHolder();
		//对歌曲列表子项布局进行缓存
		if(convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder.songImage = (ImageView) view.findViewById(R.id.song_image);
			viewHolder.songName = (TextView) view.findViewById(R.id.song_name);
			viewHolder.songDuration = (TextView) view.findViewById(R.id.song_duration);
			//public void setTag (Object tag)设置与此视图关联的标记。
			view.setTag(viewHolder);
		}else {
			view = convertView;
			//public Object getTag ()返回该视图的标记。
			//此视图中存储的对象作为标记，如果未设置则为null
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.songImage.setImageResource(listContent.getImageId());
		viewHolder.songName.setText(listContent.getSong());
		viewHolder.songDuration.setText(GetMedia.formatTime(listContent.getDuration()));
		return view;
	}
}
//内部类，对歌曲列表子项布局内的控件进行缓存
class ViewHolder{
	ImageView songImage;
	TextView songName;
	TextView songDuration;
}

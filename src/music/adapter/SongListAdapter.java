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

	int resourceId;//�����б�Ĳ���
	public SongListAdapter(Context context, int textViewResourceId, List<ListContent> objects) {
		super(context,textViewResourceId,objects);
		resourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListContent listContent = getItem(position);
		View view;
		ViewHolder viewHolder = new ViewHolder();
		//�Ը����б�����ֽ��л���
		if(convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			viewHolder.songImage = (ImageView) view.findViewById(R.id.song_image);
			viewHolder.songName = (TextView) view.findViewById(R.id.song_name);
			viewHolder.songDuration = (TextView) view.findViewById(R.id.song_duration);
			//public void setTag (Object tag)���������ͼ�����ı�ǡ�
			view.setTag(viewHolder);
		}else {
			view = convertView;
			//public Object getTag ()���ظ���ͼ�ı�ǡ�
			//����ͼ�д洢�Ķ�����Ϊ��ǣ����δ������Ϊnull
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.songImage.setImageResource(listContent.getImageId());
		viewHolder.songName.setText(listContent.getSong());
		viewHolder.songDuration.setText(GetMedia.formatTime(listContent.getDuration()));
		return view;
	}
}
//�ڲ��࣬�Ը����б�������ڵĿؼ����л���
class ViewHolder{
	ImageView songImage;
	TextView songName;
	TextView songDuration;
}

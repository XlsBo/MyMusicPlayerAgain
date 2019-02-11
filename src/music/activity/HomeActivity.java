package music.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import music.adapter.SongListAdapter;
import music.util.GetMedia;
import music.util.ListContent;

//�����棬��ʾ�����б�ѡ�񲥷�ģʽ����ʾ���Ž���
public class HomeActivity extends Activity implements OnItemClickListener{
	private static String currentSong = null;
	private static String nextSong = null;
	private List<ListContent> listContent;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);
		//��̬����Ӧ��ʹ��ClassName.staticMethod()����ʽʹ��
		listContent = GetMedia.getSongInfo(HomeActivity.this);
		SongListAdapter songListAdapter = new SongListAdapter(HomeActivity.this,R.layout.song_item,listContent);
		ListView listView = (ListView) findViewById(R.id.home_list);
		listView.setAdapter(songListAdapter);
		songListAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}

	//����listView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListContent songListContent =listContent.get(position);
		songListContent = listContent.get(position);//��ñ�����Ķ���
		//��һ�ε��������ʱ���õ�ǰ������·��
		if(currentSong == null) {
			currentSong = songListContent.getSongPath();
			Log.d("HomeActivity","currentSong" + currentSong);
			try {
				mediaPlayer.setDataSource(currentSong);
				mediaPlayer.prepare();
			}catch(Exception e) {
				e.printStackTrace();
			}
			mediaPlayer.start();
			//��һ�ε��������·��
		}else {
			nextSong = songListContent.getSongPath();
			if(currentSong != nextSong) {
				currentSong = nextSong;//�������ĸ�������һ�ε���Ĳ�һ�����򲥷ŵ�ǰ����ĸ���
				Log.d("HomeActivity","nextSong" + currentSong);
				mediaPlayer.reset();
				try {
					mediaPlayer.setDataSource(nextSong);
					mediaPlayer.prepare();
				}catch(Exception e) {
					e.printStackTrace();
				}
				mediaPlayer.start();
				//�������ĸ�������һ�ε����һ��������ͣ���������
			}else {
				if(!mediaPlayer.isPlaying()) {
					mediaPlayer.start();
				}else {
					mediaPlayer.pause();
				}
			}
		}
	}
	
	@Override
	protected void  onDestroy() {
		super.onDestroy();
		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
}

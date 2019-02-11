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

//主界面，显示歌曲列表，选择播放模式，显示播放进度
public class HomeActivity extends Activity implements OnItemClickListener{
	private static String currentSong = null;
	private static String nextSong = null;
	private List<ListContent> listContent;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);
		//静态方法应该使用ClassName.staticMethod()的形式使用
		listContent = GetMedia.getSongInfo(HomeActivity.this);
		SongListAdapter songListAdapter = new SongListAdapter(HomeActivity.this,R.layout.song_item,listContent);
		ListView listView = (ListView) findViewById(R.id.home_list);
		listView.setAdapter(songListAdapter);
		songListAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}

	//监听listView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListContent songListContent =listContent.get(position);
		songListContent = listContent.get(position);//获得被点击的对象
		//第一次点击歌曲的时候获得当前歌曲的路径
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
			//下一次点击歌曲的路径
		}else {
			nextSong = songListContent.getSongPath();
			if(currentSong != nextSong) {
				currentSong = nextSong;//如果点击的歌曲和上一次点击的不一样，则播放当前点击的歌曲
				Log.d("HomeActivity","nextSong" + currentSong);
				mediaPlayer.reset();
				try {
					mediaPlayer.setDataSource(nextSong);
					mediaPlayer.prepare();
				}catch(Exception e) {
					e.printStackTrace();
				}
				mediaPlayer.start();
				//如果点击的歌曲和上一次点击的一样，则暂停或继续播放
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

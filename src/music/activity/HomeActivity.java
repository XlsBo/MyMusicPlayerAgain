package music.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import music.adapter.SongListAdapter;
import music.util.GetMedia;
import music.util.ListContent;

//�����棬��ʾ�����б�ѡ�񲥷�ģʽ����ʾ���Ž���
public class HomeActivity extends Activity implements OnItemClickListener,OnClickListener{
	private int thePlayPosition = 0;
	private int itemPosition = -10;
	private int buttonPosition = 0;
	private static String currentSong = null;
	private static String nextSong = null;
	private List<ListContent> listContent;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private Button previous;
	private Button repeat;
	private Button play;
	private Button shuffle;
	private Button next;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout);
		//��̬����Ӧ��ʹ��ClassName.staticMethod()����ʽʹ��
		listContent = GetMedia.getSongInfo(HomeActivity.this);
		SongListAdapter songListAdapter = new SongListAdapter(HomeActivity.this,R.layout.song_item,listContent);
		ListView listView = (ListView) findViewById(R.id.home_list);
		viewFindViewById();
		viewOnClickListener();
		listView.setAdapter(songListAdapter);
		songListAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}

	//����listView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		itemPosition = position;
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
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.previous:
			previousMusic();
			break;
		case R.id.play:
			playMusic();
			break;
		case R.id.next:
			nextMusic();
			break;
		default:
			break;
		}
	}
	
	// ������һ��
	public void previousMusic() {
		ListContent previousSongContent = null;
		if(currentSong == null) {
			thePlayPosition = listContent.size()-1;
			//�������ֵ��߼�
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null) {
			if(itemPosition == -10) {
				thePlayPosition = buttonPosition;
			}else {
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			thePlayPosition--;
			if(thePlayPosition <0 ) {
				thePlayPosition = listContent.size()-1;
			}
			//��������
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}
	}
	
	//��ͣ���������
	public void playMusic() {
		if(currentSong != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}else {
				mediaPlayer.start();
			}
		}
	}
	
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {
			thePlayPosition = 0;
			//�������ֵ��߼�
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null) {
			if(itemPosition == -10) {
				thePlayPosition = buttonPosition;
			}else {
				thePlayPosition = itemPosition;
				itemPosition++;
			}
			thePlayPosition++;
			if(thePlayPosition > listContent.size()-1) {
				thePlayPosition = 0;
			}
			//��������
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}
	}
	
	public void initMediaPlayer(ListContent initSongContent,int position) {
		initSongContent = listContent.get(position);
		buttonPosition = position;
		currentSong = initSongContent.getSongPath();
		try {
			mediaPlayer.setDataSource(initSongContent.getSongPath());
			mediaPlayer.prepare();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void viewFindViewById() {
		previous = (Button) findViewById(R.id.previous);
		repeat = (Button) findViewById(R.id.repeat);
		play = (Button) findViewById(R.id.play);
		shuffle = (Button) findViewById(R.id.shuffle);
		next = (Button) findViewById(R.id.next);
	}
	
	public void viewOnClickListener() {
		previous.setOnClickListener(this);
		repeat.setOnClickListener(this);
		play.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		next.setOnClickListener(this);
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

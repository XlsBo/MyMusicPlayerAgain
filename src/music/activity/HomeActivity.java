package music.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import music.adapter.SongListAdapter;
import music.util.GetMedia;
import music.util.ListContent;

//主界面，显示歌曲列表，选择播放模式，显示播放进度
public class HomeActivity extends Activity implements OnItemClickListener,OnClickListener,OnCompletionListener{
	private int repeatModel = 0;//单曲循环
	private int shuffleModel = 0;//随机播放
	private Random shuffleRandom = new Random();;
	private int thePlayPosition = 0;
	private int itemPosition = -10;//没有点击列表播放歌曲
	private int buttonPosition = 0;//点击上一首或下一首播放歌曲
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
		//静态方法应该使用ClassName.staticMethod()的形式使用
		listContent = GetMedia.getSongInfo(HomeActivity.this);
		SongListAdapter songListAdapter = new SongListAdapter(HomeActivity.this,R.layout.song_item,listContent);
		ListView listView = (ListView) findViewById(R.id.home_list);
		viewFindViewById();
		viewOnClickListener();
		listView.setAdapter(songListAdapter);
		songListAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
		mediaPlayer.setOnCompletionListener(this);
	}

	//监听listView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		itemPosition = position;
		ListContent songListContent =listContent.get(position);
		songListContent = listContent.get(position);//获得被点击的对象
		//第一次点击歌曲的时候获得当前歌曲的路径
		if(currentSong == null) {
			currentSong = songListContent.getSongPath();
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
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.previous:
			previousMusic();
			break;
		case R.id.repeat:
			repeatModel++;
			shuffleModel = 0;
			if(repeatModel>1) {
				repeatModel = 0;
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();	
			}else {
				mediaPlayer.setLooping(true);
				Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.play:
			playMusic();
			break;
		case R.id.shuffle:
			shuffleModel++;
			repeatModel = 0;
			if(shuffleModel>1) {
				shuffleModel = 0;
				String ce0 = Integer.toString(shuffleModel);
				Log.d("HomeActivity","ce0" + ce0);
				Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();	
			}else if(shuffleModel == 1){
				String ce1 = Integer.toString(shuffleModel);
				Log.d("HomeActivity","ce1" + ce1);
				Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
			}break;
		case R.id.next:
			nextMusic();
			break;
		default:
			break;
		}
	}
	//音乐播放完成时调用
		@Override
		public void onCompletion(MediaPlayer mp) {
			ListContent completionSongContent = null;
			//随机播放
			if(shuffleModel == 1) {
				thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);//产生随机数播放音乐
				String cePosition = Integer.toString(thePlayPosition);
				Log.d("HomeActivity","cePosition" + cePosition);
				mediaPlayer.reset();
				initMediaPlayer(completionSongContent,thePlayPosition);
				mediaPlayer.start();
			}else if(mediaPlayer.isLooping() == false) {
				//点击列表播放歌曲
				if(itemPosition == -10) {
					thePlayPosition = ++buttonPosition;
					//上一首或下一首播放歌曲
				}else {
					thePlayPosition = ++itemPosition;
				}
				mediaPlayer.reset();
				initMediaPlayer(completionSongContent,thePlayPosition);
				mediaPlayer.start();
			}
		}
	// 播放上一首
	public void previousMusic() {
		ListContent previousSongContent = null;
		if(currentSong == null) {
			thePlayPosition = listContent.size()-1;
			//播放音乐的逻辑
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel != 1) {
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
			//播放音乐
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			//播放音乐
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}
	}
	
	//暂停或继续播放
	public void playMusic() {
		if(currentSong != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}else {
				mediaPlayer.start();
			}
		}
	}
	
	//单曲音乐播放完成时调用
	
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {
			thePlayPosition = 0;
			//播放音乐的逻辑
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel != 1) {
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
			//播放音乐
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			//播放音乐
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}
	}
	
	public void initMediaPlayer(ListContent initSongContent,int position) {
		initSongContent = listContent.get(position);
		buttonPosition = position;
		currentSong = initSongContent.getSongPath();
		String displaySong = initSongContent.getSong();
		Toast.makeText(this, displaySong, Toast.LENGTH_SHORT).show();
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

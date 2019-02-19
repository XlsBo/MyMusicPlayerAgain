package music.activity;

import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import music.adapter.SongListAdapter;
import music.service.PlayMusicService;
import music.util.GetMedia;
import music.util.ListContent;

//主界面，显示歌曲列表，选择播放模式，显示播放进度
public class HomeActivity extends Activity implements OnItemClickListener,OnClickListener{
	public static final String CURRENT_TIME_ACTION = "music.action.SONG_CURRENT_TIME";	
	public static final String CURRENT_TITLE_ACTION = "music.action.SONG_CURRENT_TITLE";
	public static final String CURRENT_ARTIST_ACTION="musci.acitoin.SONG_CURRENT_ARTIST";
	public static final String CURRENT_IS_PLAY = "music.action.IS_PLAY";
	public static final int RETURNPLAY = 1;
	private int buttonName = 0;          //按钮的Id
	private Intent toServiceIntent;
	private Intent nextIntent = new Intent();
	private String currentSong = null;   //当前歌曲的路径
	private String nextSong = null;      //下一首歌曲的路径
	private int thePlayPosition = 0;
	private int buttonPosition = 0;
	private int repeatModel = 0;      //单曲循环（0是false，1是true）;
	private int shuffleModel = 0;     //随机播放
	private int isPlay = 0;
	private int itemPosition = -10;//没有点击列表播放歌曲
	private List<ListContent> listContent;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private Button previous;
	private Button repeat;
	private Button play;
	private Button shuffle;
	private Button next;
	private Button playImage;   //切换活动的按钮
	private TextView songTitle;
	private TextView songDuration;
	private int itemOpen = 0;   //判断是否点击了列表上的歌曲
	private IntentFilter intentFilter; 
	private MyBroadcastReceiver mBroadcastReceiver;
	private int songCurrentTime = 0;
	private int theSongTitle = -1;
	private String equalSong = "equalSong";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		intentFilter = new IntentFilter();
		intentFilter.addAction(CURRENT_TIME_ACTION);
		intentFilter.addAction(CURRENT_TITLE_ACTION);
		mBroadcastReceiver = new MyBroadcastReceiver();
		registerReceiver(mBroadcastReceiver,intentFilter);
	}

	//监听listView
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		toServiceIntent = new Intent(this,PlayMusicService.class);
	    itemOpen = 1;                      //判断是不是点击了列表
		toServiceIntent.putExtra("itemOpen",itemOpen);
		toServiceIntent.putExtra("repeatModel", repeatModel);
		toServiceIntent.putExtra("shuffleModel", shuffleModel);
		itemPosition = position;
		ListContent songListContent =listContent.get(position);
		songListContent = listContent.get(position);//获得被点击的对象
		//第一次点击歌曲的时候获得当前歌曲的路径
		if(currentSong == null) {
			toServiceIntent.putExtra("currentSong", currentSong);
			currentSong = songListContent.getSongPath();
			toServiceIntent.putExtra("itemPosition", itemPosition);
			startService(toServiceIntent);
			//下一次点击歌曲的路径
		}else {
			nextSong = songListContent.getSongPath();
			if(currentSong != nextSong) {
				toServiceIntent.putExtra("currentSong", currentSong);
				toServiceIntent.putExtra("nextSong", nextSong);
				toServiceIntent.putExtra("itemPosition", itemPosition);
				startService(toServiceIntent);
				currentSong = nextSong;                      //如果点击的歌曲和上一次点击的不一样，则播放当前点击的歌曲
				//如果点击的歌曲和上一次点击的一样，则暂停或继续播放
			}else {
				toServiceIntent.putExtra("equalSong", equalSong);//连续两次点击同一首歌
				toServiceIntent.putExtra("currentSong", currentSong);
				toServiceIntent.putExtra("nextSong", nextSong);
				toServiceIntent.putExtra("itemPosition", itemPosition);
				startService(toServiceIntent);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent toServiceIntent = new Intent(HomeActivity.this,PlayMusicService.class);
		buttonName = v.getId();
		switch(v.getId()) {
		case R.id.previous:                        //上一首
			previous.setBackgroundResource(R.drawable.previous_selector);
			previousMusic(toServiceIntent,buttonName);
			break;
		case R.id.repeat:                          //单曲循环
			repeatModel++;
			shuffleModel = 0;
			if(repeatModel>1) {
				repeatModel = 0;
			}
			if(repeatModel == 1) {
				repeat.setBackgroundResource(R.drawable.repeat_selector);
			}else {
				repeat.setBackgroundResource(R.drawable.no_repeat_selector);
			}
			sendState(toServiceIntent);
			startService(toServiceIntent);
			break;
		case R.id.play:                            //播放与暂停
			if(currentSong != null) {
				isPlay++;
				if(isPlay>1) {
					isPlay = 0;
				}
				if(isPlay == 0) {
					play.setBackgroundResource(R.drawable.pause_selector);
				}else {
					play.setBackgroundResource(R.drawable.play_selector);
				}
				playMusic(toServiceIntent,buttonName);
			}		
			break;
		case R.id.shuffle:                         //随机播放
			shuffleModel++;
			repeatModel = 0;
			if(shuffleModel>1) {
				shuffleModel = 0;
			}
			if(shuffleModel == 1) {
				shuffle.setBackgroundResource(R.drawable.shuffle_selector);
			}else {
				shuffle.setBackgroundResource(R.drawable.no_shuffle_selector);
			}
			sendState(toServiceIntent);
			startService(toServiceIntent);
			break;
		case R.id.next:                            //下一首
			next.setBackgroundResource(R.drawable.next_selector);
			nextMusic(toServiceIntent,buttonName);
			break;
		case R.id.play_image:
			nextIntent = new Intent(HomeActivity.this,PlayActivity.class);
			nextIntent.putExtra("theSongTitle", theSongTitle);
			sendState(nextIntent);
			startActivityForResult(nextIntent,RETURNPLAY);
			break;
		default:
			break;
		}
	}
	// 播放上一首
	public void previousMusic(Intent previousIntent,int buttonName) {
		previousIntent.putExtra("buttonName", buttonName);
		itemOpen=0;
		previousIntent.putExtra("itemOpen", itemOpen);
		ListContent previousSongContent = null;
		if(currentSong == null) {
			previousIntent.putExtra("currentSong", currentSong);
			thePlayPosition = listContent.size()-1;
			//播放音乐的逻辑
			initMediaPlayer(previousSongContent,thePlayPosition);
			previousIntent.putExtra("repeatModel", repeatModel);
			previousIntent.putExtra("buttonPosition", buttonPosition);
			previousIntent.putExtra("shffleMedol", shuffleModel);
			startService(previousIntent);
		}else if(currentSong != null && shuffleModel != 1) {
			if(itemPosition == -10){
				if(--buttonPosition<=0) {
					buttonPosition = listContent.size()-1;
				}
				thePlayPosition = buttonPosition;
			}else {
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			if(itemPosition == -1 ) {
				itemPosition = listContent.size()-1;
			}
			sendState(previousIntent);
			startService(previousIntent);
			buttonPosition--;
		}else if(currentSong != null && shuffleModel == 1) {
			sendState(previousIntent);
			startService(previousIntent);
		}
	}
	
	//暂停或继续播放
	public void playMusic(Intent playIntent,int buttonName) {
		playIntent = new Intent(HomeActivity.this,PlayMusicService.class);
		playIntent.putExtra("buttonName", buttonName);
		playIntent.putExtra("currentSong", currentSong);
		startService(playIntent);
	}
	
	//单曲音乐播放完成时调用
	
	public void nextMusic(Intent nextIntent,int buttonName) {
		nextIntent.putExtra("buttonName", buttonName);
		itemOpen=0;
		nextIntent.putExtra("itemOpen", itemOpen);
		ListContent nextSongContent = null;
		if(currentSong == null) {
			nextIntent.putExtra("currentSong", currentSong);
			nextIntent.putExtra("repeatModel", repeatModel);
			nextIntent.putExtra("shffleMedol", shuffleModel);
			startService(nextIntent);
			thePlayPosition = 0;
			//播放音乐的逻辑
			initMediaPlayer(nextSongContent,thePlayPosition);
		}else if(currentSong != null && shuffleModel != 1) {
			if(itemPosition == -10) {
				if(++buttonPosition>=listContent.size()-1) {
					buttonPosition = 0;
				}
				thePlayPosition = buttonPosition;
			}else {
				thePlayPosition = itemPosition;
				itemPosition++;
			}
			thePlayPosition++;
			if(itemPosition > listContent.size()-1 ) {
				itemPosition = 0;
			}
			sendState(nextIntent);
			startService(nextIntent);
			buttonPosition++;
		}else if(currentSong != null && shuffleModel == 1) {
			sendState(nextIntent);
			startService(nextIntent);
		}
	}
	
	//缓存歌曲
	public void initMediaPlayer(ListContent initSongContent,int position) {
		initSongContent = listContent.get(position);
		buttonPosition = position;
		currentSong = initSongContent.getSongPath();
	}
	
	public void viewFindViewById() {
		previous = (Button) findViewById(R.id.previous);
		repeat = (Button) findViewById(R.id.repeat);
		play = (Button) findViewById(R.id.play);
		shuffle = (Button) findViewById(R.id.shuffle);
		next = (Button) findViewById(R.id.next);
		songTitle = (TextView) findViewById(R.id.song_title);
		songDuration = (TextView) findViewById(R.id.song_duration);
		playImage = (Button) findViewById(R.id.play_image);
	}
	
	public void viewOnClickListener() {
		previous.setOnClickListener(this);
		repeat.setOnClickListener(this);
		play.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		next.setOnClickListener(this);
		playImage.setOnClickListener(this);
	}
	
	//获得playActivity传来的数据
	public void getState(Intent intent) {
		itemPosition = intent.getIntExtra("itemPosition", -10);
		thePlayPosition = intent.getIntExtra("thePlayPosition", 0);
		buttonPosition = intent.getIntExtra("buttonPosition", 0);
		currentSong = intent.getStringExtra("currentSong");
		nextSong = intent.getStringExtra("nextSong");
		equalSong = intent.getStringExtra("equalSong");
		repeatModel = intent.getIntExtra("repeatModel",0);
		shuffleModel = intent.getIntExtra("shuffleModel",0);
		isPlay = intent.getIntExtra("isPlay", 0);
	}
	
	//传递给service的数据
	public void sendState(Intent sendIntent) {
		sendIntent.putExtra("buttonName", buttonName);
		sendIntent.putExtra("repeatModel", repeatModel);
		sendIntent.putExtra("itemPosition", itemPosition);
		sendIntent.putExtra("currentSong", currentSong);
		sendIntent.putExtra("shuffleModel", shuffleModel);
		sendIntent.putExtra("buttonPosition", buttonPosition);
		sendIntent.putExtra("equalSong", equalSong);//连续两次点击同一首歌
		sendIntent.putExtra("currentSong", currentSong);
		sendIntent.putExtra("nextSong", nextSong);
		sendIntent.putExtra("songCurrentTime", songCurrentTime);
		sendIntent.putExtra("isPlay", isPlay);
	}
	
	//广播接收器
	class MyBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context,Intent intent) {
			ListContent receiverSongContent = null;
			String action = intent.getAction();
			if(action.equals(CURRENT_TITLE_ACTION)) {
				theSongTitle = intent.getIntExtra("theSongTitle",-1);
				receiverSongContent = listContent.get(theSongTitle);				
				songTitle.setText(receiverSongContent.getSong());
			}else if(action.equals(CURRENT_TIME_ACTION)) {
				songCurrentTime = intent.getIntExtra("songCurrentTime", -1);//接收service发来的当前播放时间
				songDuration.setText(GetMedia.formatTime(songCurrentTime));
				isPlay = intent.getIntExtra("isPlay", isPlay);
				if(isPlay == 0) {
					play.setBackgroundResource(R.drawable.pause_selector);
				}else {
					play.setBackgroundResource(R.drawable.play_selector);
				}
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		switch(requestCode) {
		case RETURNPLAY:
			if(resultCode == RESULT_OK) {
				getState(data);
				if(isPlay == 0) {
					play.setBackgroundResource(R.drawable.pause_selector);
				}else {
					play.setBackgroundResource(R.drawable.play_selector);
				}
			}
			break;
		default:
			break;
		}
	}
	
	
	@Override
	protected void  onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		Intent stopServiceIntent = new Intent(this,PlayMusicService.class);
		stopService(stopServiceIntent);
		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
}

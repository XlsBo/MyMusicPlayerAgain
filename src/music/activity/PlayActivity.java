package music.activity;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import music.service.PlayMusicService;
import music.util.GetMedia;
import music.util.ListContent;

public class PlayActivity extends Activity implements OnClickListener,OnSeekBarChangeListener{
	public static final String TAG = "PlayActivity";
	public static final String PLAY_CURRENT_TIME_ACTION = "music.action.SONG_CURRENT_TIME";
	public static final String PLAY_CURRENT_TITLE_ACTION = "music.action.SONG_CURRENT_TITLE";
	public static final String PLAY_SEEKBAR_PROGRESS_CHANGE = "music.action.PROGRESS_CHANGE";
	public static final String PLAY_IS_PLAYING= "music.action.IS_PLAYING";
	//private IntentFilter intentFilter;
	//private PlayBroadcastReceiver pBroadcast;
	private TextView playSongName;
	private TextView playSongArtist;
	private TextView startDuration;
	private TextView endDuration;
	private Button playStop;
	private Button playPrevious;
	private Button playNext;
	private SeekBar timeProgress;
	private PlayBroadcastReceiver mPlayReceiver;
	private IntentFilter intentFilter;
	private List<ListContent> listContent;
	private int theSongTitle = 0;
	private Intent frmHIntent;
	private int buttonName = 0;
	private int itemPosition = -10;
	private int thePlayPosition = 0;
	private int buttonPosition = 0;
	private int repeatModel = 0;
	private int shuffleModel = 0;
	private int isPlay = 0;
	private int songCurrentTime = 0;
	private String currentSong = null;
	private String nextSong = null;
	private String equalSong = null;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			ListContent handleSongContent = null;
			switch(msg.what){
			case 1:
				timeProgress.setProgress(songCurrentTime);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity);
		listContent = GetMedia.getSongInfo(PlayActivity.this);
		viewFindId();
		onClickListener();
		Intent initIntent = getIntent();
		frmHIntent = getIntent();
		getState(frmHIntent);
		initView(initIntent);
		intentFilter = new IntentFilter();
		mPlayReceiver = new PlayBroadcastReceiver();
		intentFilter.addAction(PLAY_CURRENT_TIME_ACTION);
		registerReceiver(mPlayReceiver,intentFilter);
	}
	
	//切歌监听
	@Override
	public void onClick(View v) {
		Intent toServiceIntent = new Intent(PlayActivity.this,PlayMusicService.class);
		ListContent clickSongContent = null;
		buttonName = v.getId();
		switch(v.getId()) {
		case R.id.play_previous:
			playPrevious.setBackgroundResource(R.drawable.previous_selector);
			previousMusic(toServiceIntent,clickSongContent);
			break;
		case R.id.play_stop:
			if(currentSong != null) {
				isPlay++;
				if(isPlay>1) {
					isPlay = 0;
				}
				if(isPlay == 0) {
					playStop.setBackgroundResource(R.drawable.pause_selector);
				}else {
					playStop.setBackgroundResource(R.drawable.play_selector);
				}
				playMusic(toServiceIntent,buttonName);
			}
			break;
		case R.id.play_next:
			playNext.setBackgroundResource(R.drawable.next_selector);
			nextMusic(toServiceIntent,clickSongContent);
			break;
		default:
			break;
		}
	}	
	//seekbar监听
	@Override
	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
		Intent seekIntent = new Intent();
		switch(seekBar.getId()) {
		case R.id.seekbar_duration:
			if(fromUser == true) {
				seekIntent.setAction(PLAY_SEEKBAR_PROGRESS_CHANGE);
				seekIntent.putExtra("progress", progress);
				sendBroadcast(seekIntent);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
	//上一首
	public void previousMusic(Intent preIntent,ListContent preSongContent) {
		if(itemPosition == -10) {
			thePlayPosition = --buttonPosition;
		}else if(itemPosition != -10) {
			thePlayPosition = --itemPosition;
		}
		if(thePlayPosition < 0) {
			thePlayPosition = listContent.size()-1;
		}else if(thePlayPosition > listContent.size()-1) {
			thePlayPosition = 0;
		}
		
		if(itemPosition < 0&&itemPosition != -10) {
			itemPosition = listContent.size()-1;
		}else if(itemPosition > listContent.size()-1) {
			itemPosition = 0;
		}
		
		if(buttonPosition < 0) {
			buttonPosition = listContent.size()-1;
		}else if(buttonPosition > listContent.size()-1) {
			buttonPosition = 0;
		}
		preSongContent = listContent.get(thePlayPosition);
		playSongName.setText(preSongContent.getSong());
		playSongArtist.setText(preSongContent.getSongArtist());
		endDuration.setText(GetMedia.formatTime(preSongContent.getDuration()));
		timeProgress.setMax(preSongContent.getDuration());
		sendState(preIntent);
		startService(preIntent);
	}
	
	//暂停或继续播放
		public void playMusic(Intent playIntent,int buttonName) {
			playIntent = new Intent(PlayActivity.this,PlayMusicService.class);
			playIntent.putExtra("buttonName", buttonName);
			playIntent.putExtra("currentSong", currentSong);
			startService(playIntent);
		}
	
	//下一首
	public void nextMusic(Intent nextIntent,ListContent nextSongContent) {
		if(itemPosition == -10) {
			thePlayPosition = ++buttonPosition;
		}else if(itemPosition != -10) {
			thePlayPosition = ++itemPosition;
		}
		if(thePlayPosition < 0) {
			thePlayPosition = listContent.size()-1;
		}else if(thePlayPosition > listContent.size()-1) {
			thePlayPosition = 0;
		}
		
		if(itemPosition < 0 && itemPosition != -10) {
			itemPosition = listContent.size()-1;
		}else if(itemPosition > listContent.size()-1) {
			itemPosition = 0;
		}
		
		if(buttonPosition < 0) {
			buttonPosition = listContent.size()-1;
		}else if(buttonPosition > listContent.size()-1) {
			buttonPosition = 0;
		}

		nextSongContent = listContent.get(thePlayPosition);
		playSongName.setText(nextSongContent.getSong());
		playSongArtist.setText(nextSongContent.getSongArtist());
		endDuration.setText(GetMedia.formatTime(nextSongContent.getDuration()));
		timeProgress.setMax(nextSongContent.getDuration());
		sendState(nextIntent);
		startService(nextIntent);
	}
	
	@Override
	public void onBackPressed() {
		Intent backIntent = new Intent();
		sendState(backIntent);
		setResult(RESULT_OK,backIntent);
		finish();
		
	}
	
	//获得HomeActivity传来的数据
	public void getState(Intent intent) {
		itemPosition = intent.getIntExtra("itemPosition", -10);
		thePlayPosition = intent.getIntExtra("thePlayPosition", 0);
		buttonPosition = intent.getIntExtra("buttonPosition", 0);
		currentSong = intent.getStringExtra("currentSong");
		nextSong = intent.getStringExtra("nextSong");
		equalSong = intent.getStringExtra("equalSong");
		repeatModel = intent.getIntExtra("repeatModel",0);
		shuffleModel = intent.getIntExtra("shuffleModel",0);
		songCurrentTime = intent.getIntExtra("songCurrentTIme",0);
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
			sendIntent.putExtra("equalSong", "equalSong");//连续两次点击同一首歌
			sendIntent.putExtra("currentSong", currentSong);
			sendIntent.putExtra("nextSong", nextSong);
			sendIntent.putExtra("isPlay", isPlay);
		}
	public void viewFindId() {
		playSongName = (TextView) findViewById(R.id.play_song_name);
		playSongArtist = (TextView) findViewById(R.id.play_song_artist);
		startDuration = (TextView) findViewById(R.id.start_duration);
		endDuration = (TextView) findViewById(R.id.end_duration);
		playStop = (Button) findViewById(R.id.play_stop);
		playPrevious = (Button) findViewById(R.id.play_previous);
		playNext = (Button) findViewById(R.id.play_next);
		timeProgress = (SeekBar) findViewById(R.id.seekbar_duration);
	}
	
	public void onClickListener() {
		playStop.setOnClickListener(this);
		playPrevious.setOnClickListener(this);
		playNext.setOnClickListener(this);
		timeProgress.setOnSeekBarChangeListener(this);
	}
	
	public void initView(Intent headIntent) {
		ListContent headSongContent = null;
		theSongTitle = headIntent.getIntExtra("theSongTitle", -1);
		headSongContent = listContent.get(thePlayPosition);
		isPlay = headIntent.getIntExtra("isPlay", 0);
		if(isPlay == 0) {
			playStop.setBackgroundResource(R.drawable.pause_selector);
		}else {
			playStop.setBackgroundResource(R.drawable.play_selector);
		}
		if(theSongTitle != -1) {
			headSongContent = listContent.get(theSongTitle);
			playSongName.setText(headSongContent.getSong());
			playSongArtist.setText(headSongContent.getSongArtist());
			timeProgress.setMax(headSongContent.getDuration());
			endDuration.setText(GetMedia.formatTime(headSongContent.getDuration()));
		}

		
	}
	
	class PlayBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context,Intent intent) {
			ListContent reSongContent = null;
			String mAction = intent.getAction();
			int theSongTitle = intent.getIntExtra("theSongTitle", -1);
			if(mAction.equals(PLAY_CURRENT_TIME_ACTION)) {
				reSongContent = listContent.get(thePlayPosition);
				songCurrentTime = intent.getIntExtra("songCurrentTime", 0);
				startDuration.setText(GetMedia.formatTime(songCurrentTime));
				handler.sendEmptyMessage(1);
			}
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mPlayReceiver);
	}
}

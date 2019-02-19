package music.service;

import java.util.List;
import java.util.Random;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import music.activity.R;
import music.util.GetMedia;
import music.util.ListContent;

public class PlayMusicService extends Service implements OnCompletionListener{
	public static final String SERVICE_CURRENT_TIME_ACTION = "music.action.SONG_CURRENT_TIME";
	public static final String SERVICE_CURRENT_TITLE_ACTION = "music.action.SONG_CURRENT_TITLE";
	public static final String SERVICE_SEEKBAR_PROGRESS_CHANGE = "music.action.PROGRESS_CHANGE";
	public static final String SERVICE_IS_PLAY = "music.action.IS_PLAY";
	private Intent tdIntent = new Intent();
	public static final String TAG = "PlayMusicService";
	private int buttonName = 0;                    //按钮的id
	private Random shuffleRandom = new Random();   //随机获得歌曲的位置
	private int itemOpen = 0;                      //判断是否点击了歌曲列表
	private int repeatModel = 0;                   //单曲循环
	private int shuffleModel = 0;                  //随机播放
	private int isPlay = 0;
	private int thePlayPosition = 0;               //当前播放歌曲的位置
	private int buttonPosition = 0;                //点击按钮后歌曲的位置
	private List<ListContent> listContent;
	private String currentSong = null;             //当前歌曲的路径
	private String nextSong = null;                //下一首歌曲的路径，仅在itemClick中使用
	private String equalSong = null;               //currentSong和nextSong相不相等的判断
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private int itemPosition = -10;                //点击列表歌曲的位置
	private int songCurrentTime = 0;
	private ServiceBroadcastReceiver serBroad;
	private IntentFilter intentFilter;
	
	
	//实现计时功能
	private Handler handler = new Handler() {
		public void handleMessage(Message msg){
			if(msg.what == 1) {
				if(mediaPlayer != null) {
					Intent intent = new Intent();
					songCurrentTime = mediaPlayer.getCurrentPosition();
					intent.setAction(SERVICE_CURRENT_TIME_ACTION);
					if(mediaPlayer.isPlaying() == true) {
						isPlay = 1;
					}else {
						isPlay = 0;
					}
					intent.putExtra("songCurrentTime", songCurrentTime);
					intent.putExtra("isPlay", isPlay);
					sendBroadcast(intent);
					handler.sendEmptyMessageDelayed(1, 1000);
				}	
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//listContent要在onCreate中引用GetMedia.getSongInfo(Context context)才能得到context
		listContent = GetMedia.getSongInfo(PlayMusicService.this);
		handler.sendEmptyMessage(1);
		serBroad = new ServiceBroadcastReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(SERVICE_SEEKBAR_PROGRESS_CHANGE);
		registerReceiver(serBroad,intentFilter);
		super.onCreate();
	}
	
	//音乐播放完成时调用
			@Override
			public void onCompletion(MediaPlayer mp) {
					ListContent completionSongContent = null;
					//随机播放
					if(shuffleModel == 1 && repeatModel == 0) {       //单曲循环完成是回调
						thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);//产生随机数播放音乐
						tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
						tdIntent.putExtra("theSongTitle", thePlayPosition); 
						sendBroadcast(tdIntent);
						mediaPlayer.reset();
						initMediaPlayer(completionSongContent,thePlayPosition);
						mediaPlayer.start();
					}else if(shuffleModel !=1 && repeatModel == 0) {    //顺序播放完成是回调
						//点击列表播放歌曲
						if(itemPosition == -10) {
							thePlayPosition = ++buttonPosition;
								//上一首或下一首播放歌曲
						}else {
							thePlayPosition = ++itemPosition;
						}
						if(thePlayPosition<0) {
							thePlayPosition = listContent.size()-1;
						}else if(thePlayPosition>listContent.size()-1) {
							thePlayPosition = 0;
						}						
						tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
						tdIntent.putExtra("theSongTitle", thePlayPosition);
						sendBroadcast(tdIntent);
						mediaPlayer.reset();
						initMediaPlayer(completionSongContent,thePlayPosition);
						mediaPlayer.start();
						}
			}
	
	@Override
	public int onStartCommand(Intent intent,int flags,int startId) {
		getState(intent);    //获得主界面传来的数据
		mediaPlayer.setOnCompletionListener(this);
		ListContent songListContent = null;
		if(itemOpen == 1) {
			itemClick(songListContent,itemPosition);
			//handler.sendEmptyMessage(1);
		}
		switch(buttonName) {
		case R.id.previous:
		case R.id.play_previous:
			previousMusic();
			break;
		case R.id.repeat:
			if(repeatModel == 0){
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(true);
				Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.next:
		case R.id.play_next:
			nextMusic();
			break;
		case R.id.shuffle:
			if(shuffleModel == 0){
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.play:
		case R.id.play_stop:
			playMusic();
			break;
		default:
			break;
		}
		return super.onStartCommand(intent,flags,startId);
	}
	
	//暂停或继续播放
	public void playMusic() {
		if(currentSong != null) {
			Log.d(TAG,"ccc "+currentSong);
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				Toast.makeText(this, "暂停播放", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.start();
				Toast.makeText(this, "继续播放", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//切换到上一首
	public void previousMusic() {
		ListContent previousSongContent = null;
		if(currentSong == null) {     //没有点击列表，同时是第一次点击previous
			Log.d(TAG,"tpp1 "+ thePlayPosition);
			thePlayPosition = listContent.size()-1;
			//播放音乐的逻辑
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
			if(repeatModel == 1) {
				mediaPlayer.setLooping(true);
			}else {
				mediaPlayer.setLooping(false);
			}
		}else if(currentSong != null && shuffleModel == 0 && repeatModel == 0) {
			Log.d(TAG,"tpp2 "+ thePlayPosition);
			Log.d(TAG,"itp1 "+ itemPosition);//下一次点击previous
			if(itemPosition == -10) {                            //没有点击列表
				Log.d(TAG,"tpp3 "+ thePlayPosition);
				thePlayPosition = buttonPosition;
			}else {                                               //点击了列表
				Log.d(TAG,"itp2 "+ itemPosition);
				thePlayPosition = itemPosition;
				itemPosition--;
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
		}else if(currentSong != null && repeatModel == 1) {
			thePlayPosition = itemPosition--;
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
		tdIntent.putExtra("theSongTitle", thePlayPosition);
		sendBroadcast(tdIntent);
	}
	
	//播放下一首
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {     //没有点击列表，同时是第一次点击previous
			thePlayPosition = 0;
			//播放音乐的逻辑
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
			if(repeatModel == 1) {
				mediaPlayer.setLooping(true);
			}else {
				mediaPlayer.setLooping(false);
			}
		}else if(currentSong != null && shuffleModel != 1 && repeatModel == 0) {     //下一次点击previous
			Log.d(TAG,"shuffleModel "+shuffleModel);
			if(itemPosition == -10) {                            //没有点击列表
				Log.d(TAG,"itemPosition == -10");
				thePlayPosition = buttonPosition;
			}else {                                               //点击了列表
				thePlayPosition = itemPosition;
				itemPosition++;
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
		}else if(currentSong != null && repeatModel == 1) {
			thePlayPosition = itemPosition++;
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
		tdIntent.putExtra("theSongTitle", thePlayPosition);
		sendBroadcast(tdIntent);
	}
	
	
	//点击列表上的歌曲
	public void itemClick(ListContent itemSongContent,int itemPosition) {
		itemSongContent = listContent.get(itemPosition);
		tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
		tdIntent.putExtra("theSongTitle", itemPosition);
		sendBroadcast(tdIntent);
		if(currentSong == null) {   //第一次点击列表歌曲
			currentSong = itemSongContent.getSongPath();
			mediaPlayer.reset();
			try {
				mediaPlayer.setDataSource(currentSong);
				mediaPlayer.prepare();
			}catch(Exception e) {
				e.printStackTrace();
			}
			mediaPlayer.start();
			Toast.makeText(this, itemSongContent.getSong(), Toast.LENGTH_SHORT).show();
			if(repeatModel == 1) {
				mediaPlayer.setLooping(true);         //setLooping要在有歌曲在播放时使用才有效
			}else {
				mediaPlayer.setLooping(false);
			}
		}else if(currentSong != null && equalSong == null) {//equalSong!=null本来是currentSong和nextSong相等时额equalSong=null， 
			mediaPlayer.reset();                               //但软件不知道为什么相反了  
			currentSong = nextSong;
			try {
				mediaPlayer.setDataSource(nextSong);
				mediaPlayer.prepare();
			}catch(Exception e) {
				e.printStackTrace();
			}
			mediaPlayer.start();
			Toast.makeText(this, itemSongContent.getSong(), Toast.LENGTH_SHORT).show();
		}else if(currentSong != null && equalSong != null) {    //equalSong == null;
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}else {
				mediaPlayer.start();
			}
		}
	}
	
	//缓存歌曲
	public void initMediaPlayer(ListContent initSongContent,int position) {
		initSongContent = listContent.get(position);
		buttonPosition = position;
		currentSong = initSongContent.getSongPath();//获得当前播放歌曲的路径
		String displaySong = initSongContent.getSong();
		Toast.makeText(this, displaySong, Toast.LENGTH_SHORT).show();
		try {
			mediaPlayer.setDataSource(initSongContent.getSongPath());
			mediaPlayer.prepare();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//获得主界面传来的数据
	public void getState(Intent intent) {
		repeatModel = intent.getIntExtra("repeatModel", 0);
		shuffleModel = intent.getIntExtra("shuffleModel", 0);
		buttonPosition = intent.getIntExtra("buttonPosition", 0);
		thePlayPosition = intent.getIntExtra("thePlayPosition", 0);
		shuffleModel = intent.getIntExtra("shuffleModel", 0);
		buttonName = intent.getIntExtra("buttonName",0);
		itemOpen = intent.getIntExtra("itemOpen",0);
		itemPosition = intent.getIntExtra("itemPosition", -10);//获得列表歌曲的位置
		currentSong = intent.getStringExtra("currentSong");//获取当前播放歌曲的路径
		nextSong = intent.getStringExtra("nextSong");//获得下一次点击列表歌曲的路径
		equalSong = intent.getStringExtra("equalSong");
	}
	
	class ServiceBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context,Intent intent) {
			String mAction = intent.getAction();
			int progress = intent.getIntExtra("progress", -1);
			if(mAction == SERVICE_SEEKBAR_PROGRESS_CHANGE) {
				mediaPlayer.seekTo(progress);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		unregisterReceiver(serBroad);
	}

}

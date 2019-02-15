package music.service;

import java.util.List;
import java.util.Random;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import music.activity.R;
import music.util.GetMedia;
import music.util.ListContent;

public class PlayMusicService extends Service implements OnCompletionListener{
	public static final String TAG = "PlayMusicService";
	private int buttonName = 0;                    //按钮的id
	private Random shuffleRandom = new Random();   //随机获得歌曲的位置
	private int itemOpen = 0;                      //判断是否点击了歌曲列表
	private int repeatModel = 0;                   //单曲循环
	private int shuffleModel = 0;                  //随机播放
	private int thePlayPosition = 0;               //当前播放歌曲的位置
	private int buttonPosition = 0;                //点击按钮后歌曲的位置
	private List<ListContent> listContent;
	private String currentSong = null;             //当前歌曲的路径
	private String nextSong = null;                //下一首歌曲的路径，仅在itemClick中使用
	private String equalSong = null;               //currentSong和nextSong相不相等的判断
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private int itemPosition = -10;                //点击列表歌曲的位置
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//listContent要在onCreate中引用GetMedia.getSongInfo(Context context)才能得到context
		listContent = GetMedia.getSongInfo(PlayMusicService.this);
		//mediaPlayer.setOnCompletionListener(this);
		super.onCreate();
	}
	
	//音乐播放完成时调用
			@Override
			public void onCompletion(MediaPlayer mp) {
				ListContent completionSongContent = null;
				//随机播放
				Log.d(TAG,shuffleModel+""+repeatModel);
				if(shuffleModel == 1 && repeatModel == 0) {
					thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);//产生随机数播放音乐
					mediaPlayer.reset();
					initMediaPlayer(completionSongContent,thePlayPosition);
					mediaPlayer.start();
				}else if(shuffleModel !=1 && repeatModel == 0) {
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
				}else if(shuffleModel !=1 && repeatModel == 1) {
					
				}
			}
	
	@Override
	public int onStartCommand(Intent intent,int flags,int startId) {
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
		mediaPlayer.setOnCompletionListener(this);
		ListContent songListContent = null;
		if(itemOpen == 1) {
			Log.d(TAG,"button!=");
			itemClick(songListContent,itemPosition);
		}
		switch(buttonName) {
		case R.id.previous:
			previousMusic();
			break;
		case R.id.repeat:
			if(repeatModel == 0){
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(true);
				Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
				Log.d(TAG,"repeatModel " + repeatModel);
			}
			break;
		case R.id.next:
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
			Log.d(TAG,"shi");
			thePlayPosition = listContent.size()-1;
			//播放音乐的逻辑
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 0 && repeatModel == 0) {     //下一次点击previous
			Log.d(TAG,"shuffleModel "+shuffleModel);
			if(itemPosition == -10) {                            //没有点击列表
				Log.d(TAG,"buttonPosition 2" + buttonPosition);
				Log.d(TAG,"itemPosition 2" + itemPosition);
				thePlayPosition = buttonPosition;
			}else {                                               //点击了列表
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			//播放音乐
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			Log.d(TAG,"nextshuffle " + thePlayPosition);
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
	}
	
	//播放下一首
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {     //没有点击列表，同时是第一次点击previous
			Log.d(TAG,"shi");
			thePlayPosition = 0;
			//播放音乐的逻辑
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
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
			Log.d(TAG,"nextshuffle " + thePlayPosition);
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
	}
	
	
	//点击列表上的歌曲
	public void itemClick(ListContent itemSongContent,int itemPosition) {
		itemSongContent = listContent.get(itemPosition);
		if(currentSong == null) {                      //第一次点击列表歌曲
			Log.d(TAG,"1 ");
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
		}else if(currentSong != null && equalSong == null) {//equalSong!=null本来是currentSong和nextSong相等时额equalSong=null，但软件
			Log.d(TAG,"buxiangdeng");                        //不知道为什么相反了   
			mediaPlayer.reset();
			currentSong = nextSong;
			try {
				mediaPlayer.setDataSource(nextSong);
				mediaPlayer.prepare();
			}catch(Exception e) {
				e.printStackTrace();
			}
			mediaPlayer.start();
			Toast.makeText(this, itemSongContent.getSong(), Toast.LENGTH_SHORT).show();
		}else if(currentSong != null && equalSong != null) {//equalSong == null;
			Log.d(TAG,"playing");
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

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

}

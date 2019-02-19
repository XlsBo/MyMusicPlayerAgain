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
	private int buttonName = 0;                    //��ť��id
	private Random shuffleRandom = new Random();   //�����ø�����λ��
	private int itemOpen = 0;                      //�ж��Ƿ����˸����б�
	private int repeatModel = 0;                   //����ѭ��
	private int shuffleModel = 0;                  //�������
	private int isPlay = 0;
	private int thePlayPosition = 0;               //��ǰ���Ÿ�����λ��
	private int buttonPosition = 0;                //�����ť�������λ��
	private List<ListContent> listContent;
	private String currentSong = null;             //��ǰ������·��
	private String nextSong = null;                //��һ�׸�����·��������itemClick��ʹ��
	private String equalSong = null;               //currentSong��nextSong�಻��ȵ��ж�
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private int itemPosition = -10;                //����б������λ��
	private int songCurrentTime = 0;
	private ServiceBroadcastReceiver serBroad;
	private IntentFilter intentFilter;
	
	
	//ʵ�ּ�ʱ����
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
		//listContentҪ��onCreate������GetMedia.getSongInfo(Context context)���ܵõ�context
		listContent = GetMedia.getSongInfo(PlayMusicService.this);
		handler.sendEmptyMessage(1);
		serBroad = new ServiceBroadcastReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(SERVICE_SEEKBAR_PROGRESS_CHANGE);
		registerReceiver(serBroad,intentFilter);
		super.onCreate();
	}
	
	//���ֲ������ʱ����
			@Override
			public void onCompletion(MediaPlayer mp) {
					ListContent completionSongContent = null;
					//�������
					if(shuffleModel == 1 && repeatModel == 0) {       //����ѭ������ǻص�
						thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);//�����������������
						tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
						tdIntent.putExtra("theSongTitle", thePlayPosition); 
						sendBroadcast(tdIntent);
						mediaPlayer.reset();
						initMediaPlayer(completionSongContent,thePlayPosition);
						mediaPlayer.start();
					}else if(shuffleModel !=1 && repeatModel == 0) {    //˳�򲥷�����ǻص�
						//����б��Ÿ���
						if(itemPosition == -10) {
							thePlayPosition = ++buttonPosition;
								//��һ�׻���һ�ײ��Ÿ���
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
		getState(intent);    //��������洫��������
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
				Toast.makeText(this, "˳�򲥷�", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(true);
				Toast.makeText(this, "����ѭ��", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.next:
		case R.id.play_next:
			nextMusic();
			break;
		case R.id.shuffle:
			if(shuffleModel == 0){
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "˳�򲥷�", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(false);
				Toast.makeText(this, "�������", Toast.LENGTH_SHORT).show();
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
	
	//��ͣ���������
	public void playMusic() {
		if(currentSong != null) {
			Log.d(TAG,"ccc "+currentSong);
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				Toast.makeText(this, "��ͣ����", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.start();
				Toast.makeText(this, "��������", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//�л�����һ��
	public void previousMusic() {
		ListContent previousSongContent = null;
		if(currentSong == null) {     //û�е���б�ͬʱ�ǵ�һ�ε��previous
			Log.d(TAG,"tpp1 "+ thePlayPosition);
			thePlayPosition = listContent.size()-1;
			//�������ֵ��߼�
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
			Log.d(TAG,"itp1 "+ itemPosition);//��һ�ε��previous
			if(itemPosition == -10) {                            //û�е���б�
				Log.d(TAG,"tpp3 "+ thePlayPosition);
				thePlayPosition = buttonPosition;
			}else {                                               //������б�
				Log.d(TAG,"itp2 "+ itemPosition);
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			//��������
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			//��������
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
	
	//������һ��
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {     //û�е���б�ͬʱ�ǵ�һ�ε��previous
			thePlayPosition = 0;
			//�������ֵ��߼�
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
			if(repeatModel == 1) {
				mediaPlayer.setLooping(true);
			}else {
				mediaPlayer.setLooping(false);
			}
		}else if(currentSong != null && shuffleModel != 1 && repeatModel == 0) {     //��һ�ε��previous
			Log.d(TAG,"shuffleModel "+shuffleModel);
			if(itemPosition == -10) {                            //û�е���б�
				Log.d(TAG,"itemPosition == -10");
				thePlayPosition = buttonPosition;
			}else {                                               //������б�
				thePlayPosition = itemPosition;
				itemPosition++;
			}
			//��������
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			//��������
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
	
	
	//����б��ϵĸ���
	public void itemClick(ListContent itemSongContent,int itemPosition) {
		itemSongContent = listContent.get(itemPosition);
		tdIntent.setAction(SERVICE_CURRENT_TITLE_ACTION);
		tdIntent.putExtra("theSongTitle", itemPosition);
		sendBroadcast(tdIntent);
		if(currentSong == null) {   //��һ�ε���б����
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
				mediaPlayer.setLooping(true);         //setLoopingҪ���и����ڲ���ʱʹ�ò���Ч
			}else {
				mediaPlayer.setLooping(false);
			}
		}else if(currentSong != null && equalSong == null) {//equalSong!=null������currentSong��nextSong���ʱ��equalSong=null�� 
			mediaPlayer.reset();                               //�������֪��Ϊʲô�෴��  
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
	
	//�������
	public void initMediaPlayer(ListContent initSongContent,int position) {
		initSongContent = listContent.get(position);
		buttonPosition = position;
		currentSong = initSongContent.getSongPath();//��õ�ǰ���Ÿ�����·��
		String displaySong = initSongContent.getSong();
		Toast.makeText(this, displaySong, Toast.LENGTH_SHORT).show();
		try {
			mediaPlayer.setDataSource(initSongContent.getSongPath());
			mediaPlayer.prepare();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//��������洫��������
	public void getState(Intent intent) {
		repeatModel = intent.getIntExtra("repeatModel", 0);
		shuffleModel = intent.getIntExtra("shuffleModel", 0);
		buttonPosition = intent.getIntExtra("buttonPosition", 0);
		thePlayPosition = intent.getIntExtra("thePlayPosition", 0);
		shuffleModel = intent.getIntExtra("shuffleModel", 0);
		buttonName = intent.getIntExtra("buttonName",0);
		itemOpen = intent.getIntExtra("itemOpen",0);
		itemPosition = intent.getIntExtra("itemPosition", -10);//����б������λ��
		currentSong = intent.getStringExtra("currentSong");//��ȡ��ǰ���Ÿ�����·��
		nextSong = intent.getStringExtra("nextSong");//�����һ�ε���б������·��
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

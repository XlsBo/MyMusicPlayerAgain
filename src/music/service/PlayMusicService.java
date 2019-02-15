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
	private int buttonName = 0;                    //��ť��id
	private Random shuffleRandom = new Random();   //�����ø�����λ��
	private int itemOpen = 0;                      //�ж��Ƿ����˸����б�
	private int repeatModel = 0;                   //����ѭ��
	private int shuffleModel = 0;                  //�������
	private int thePlayPosition = 0;               //��ǰ���Ÿ�����λ��
	private int buttonPosition = 0;                //�����ť�������λ��
	private List<ListContent> listContent;
	private String currentSong = null;             //��ǰ������·��
	private String nextSong = null;                //��һ�׸�����·��������itemClick��ʹ��
	private String equalSong = null;               //currentSong��nextSong�಻��ȵ��ж�
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private int itemPosition = -10;                //����б������λ��
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//listContentҪ��onCreate������GetMedia.getSongInfo(Context context)���ܵõ�context
		listContent = GetMedia.getSongInfo(PlayMusicService.this);
		//mediaPlayer.setOnCompletionListener(this);
		super.onCreate();
	}
	
	//���ֲ������ʱ����
			@Override
			public void onCompletion(MediaPlayer mp) {
				ListContent completionSongContent = null;
				//�������
				Log.d(TAG,shuffleModel+""+repeatModel);
				if(shuffleModel == 1 && repeatModel == 0) {
					thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);//�����������������
					mediaPlayer.reset();
					initMediaPlayer(completionSongContent,thePlayPosition);
					mediaPlayer.start();
				}else if(shuffleModel !=1 && repeatModel == 0) {
					//����б��Ÿ���
					if(itemPosition == -10) {
						thePlayPosition = ++buttonPosition;
						//��һ�׻���һ�ײ��Ÿ���
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
		itemPosition = intent.getIntExtra("itemPosition", -10);//����б������λ��
		currentSong = intent.getStringExtra("currentSong");//��ȡ��ǰ���Ÿ�����·��
		nextSong = intent.getStringExtra("nextSong");//�����һ�ε���б������·��
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
				Toast.makeText(this, "˳�򲥷�", Toast.LENGTH_SHORT).show();
			}else {
				mediaPlayer.setLooping(true);
				Toast.makeText(this, "����ѭ��", Toast.LENGTH_SHORT).show();
				Log.d(TAG,"repeatModel " + repeatModel);
			}
			break;
		case R.id.next:
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
			Log.d(TAG,"shi");
			thePlayPosition = listContent.size()-1;
			//�������ֵ��߼�
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 0 && repeatModel == 0) {     //��һ�ε��previous
			Log.d(TAG,"shuffleModel "+shuffleModel);
			if(itemPosition == -10) {                            //û�е���б�
				Log.d(TAG,"buttonPosition 2" + buttonPosition);
				Log.d(TAG,"itemPosition 2" + itemPosition);
				thePlayPosition = buttonPosition;
			}else {                                               //������б�
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			//��������
			mediaPlayer.reset();
			initMediaPlayer(previousSongContent,thePlayPosition);
			mediaPlayer.start();
		}else if(currentSong != null && shuffleModel == 1) {
			thePlayPosition = shuffleRandom.nextInt(listContent.size()-1);
			Log.d(TAG,"nextshuffle " + thePlayPosition);
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
	}
	
	//������һ��
	public void nextMusic() {
		ListContent nextSongContent = null;
		if(currentSong == null) {     //û�е���б�ͬʱ�ǵ�һ�ε��previous
			Log.d(TAG,"shi");
			thePlayPosition = 0;
			//�������ֵ��߼�
			mediaPlayer.reset();
			initMediaPlayer(nextSongContent,thePlayPosition);
			mediaPlayer.start();
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
			Log.d(TAG,"nextshuffle " + thePlayPosition);
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
	}
	
	
	//����б��ϵĸ���
	public void itemClick(ListContent itemSongContent,int itemPosition) {
		itemSongContent = listContent.get(itemPosition);
		if(currentSong == null) {                      //��һ�ε���б����
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
		}else if(currentSong != null && equalSong == null) {//equalSong!=null������currentSong��nextSong���ʱ��equalSong=null�������
			Log.d(TAG,"buxiangdeng");                        //��֪��Ϊʲô�෴��   
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

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

}

package music.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
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
import music.service.PlayMusicService;
import music.util.GetMedia;
import music.util.ListContent;

//�����棬��ʾ�����б�ѡ�񲥷�ģʽ����ʾ���Ž���
public class HomeActivity extends Activity implements OnItemClickListener,OnClickListener{
	private int buttonName = 0;          //��ť��Id
	private Intent toServiceIntent;
	private String currentSong = null;   //��ǰ������·��
	private String nextSong = null;      //��һ�׸�����·��
	private int thePlayPosition = 0;
	private int buttonPosition = 0;
	private Random shuffleRandom = new Random();
	private int repeatModel = 0;      //����ѭ����0��false��1��true��;
	private int shuffleModel = 0;     //�������
	private int itemPosition = -10;//û�е���б��Ÿ���
	private List<ListContent> listContent;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private Button previous;
	private Button repeat;
	private Button play;
	private Button shuffle;
	private Button next;
	private int itemOpen = 0;   //�ж��Ƿ������б��ϵĸ���
	
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
		toServiceIntent = new Intent(this,PlayMusicService.class);
	    itemOpen = 1;                      //�ж��ǲ��ǵ�����б�
		toServiceIntent.putExtra("itemOpen",itemOpen);
		toServiceIntent.putExtra("repeatModel", repeatModel);
		toServiceIntent.putExtra("shuffleModel", shuffleModel);
		itemPosition = position;
		ListContent songListContent =listContent.get(position);
		songListContent = listContent.get(position);//��ñ�����Ķ���
		//��һ�ε��������ʱ���õ�ǰ������·��
		if(currentSong == null) {
			toServiceIntent.putExtra("currentSong", currentSong);
			currentSong = songListContent.getSongPath();
			toServiceIntent.putExtra("itemPosition", itemPosition);
			startService(toServiceIntent);
			//��һ�ε��������·��
		}else {
			nextSong = songListContent.getSongPath();
			if(currentSong != nextSong) {
				toServiceIntent.putExtra("currentSong", currentSong);
				toServiceIntent.putExtra("nextSong", nextSong);
				toServiceIntent.putExtra("itemPosition", itemPosition);
				startService(toServiceIntent);
				currentSong = nextSong;                      //�������ĸ�������һ�ε���Ĳ�һ�����򲥷ŵ�ǰ����ĸ���
				//�������ĸ�������һ�ε����һ��������ͣ���������
			}else {
				toServiceIntent.putExtra("equalSong", "equalSong");//�������ε��ͬһ�׸�
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
		case R.id.previous:
			previousMusic(toServiceIntent,buttonName);
			break;
		case R.id.repeat:
			repeatModel++;
			shuffleModel = 0;
			if(repeatModel>1) {
				repeatModel = 0;
			}
			sendState(toServiceIntent);
			startService(toServiceIntent);
			break;
		case R.id.play:
			playMusic(toServiceIntent,buttonName);
			break;
		case R.id.shuffle:
			shuffleModel++;
			repeatModel = 0;
			if(shuffleModel>1) {
				shuffleModel = 0;
			}
			sendState(toServiceIntent);
			startService(toServiceIntent);
			break;
		case R.id.next:
			nextMusic(toServiceIntent,buttonName);
			break;
		default:
			break;
		}
	}
	// ������һ��
	public void previousMusic(Intent previousIntent,int buttonName) {
		previousIntent.putExtra("buttonName", buttonName);
		itemOpen=0;
		previousIntent.putExtra("itemOpen", itemOpen);
		ListContent previousSongContent = null;
		if(currentSong == null) {
			previousIntent.putExtra("currentSong", currentSong);
			thePlayPosition = listContent.size()-1;
			//�������ֵ��߼�
			initMediaPlayer(previousSongContent,thePlayPosition);
			previousIntent.putExtra("buttonPosition", buttonPosition);
			startService(previousIntent);
		}else if(currentSong != null && shuffleModel != 1) {
			if(itemPosition == -10){
				Log.d("HomeActivity","buttonPosition 0 "+ buttonPosition);
				thePlayPosition = --buttonPosition;
				Log.d("HomeActivity","itemPosition 1 "+ itemPosition);
			}else {
				thePlayPosition = itemPosition;
				itemPosition--;
			}
			if(itemPosition == -1 ) {
				itemPosition = listContent.size()-1;
			}
			/*if(thePlayPosition <0 ) {
				thePlayPosition = listContent.size()-1;
			}*/
			/*nextIntent.putExtra("repeatModel", repeatModel);
			nextIntent.putExtra("itemPosition", itemPosition);
			nextIntent.putExtra("currentSong", currentSong);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("buttonPosition", buttonPosition);*/
			sendState(previousIntent);
			startService(previousIntent);
			buttonPosition--;
		}else if(currentSong != null && shuffleModel == 1) {
			/*nextIntent.putExtra("repeatModel", repeatModel);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("currentSong", currentSong);
			nextIntent.putExtra("buttonPosition", buttonPosition);*/
			sendState(previousIntent);
			startService(previousIntent);
		}
	}
	
	//��ͣ���������
	public void playMusic(Intent playIntent,int buttonName) {
		playIntent = new Intent(HomeActivity.this,PlayMusicService.class);
		playIntent.putExtra("buttonName", buttonName);
		playIntent.putExtra("currentSong", currentSong);
		startService(playIntent);
	}
	
	//�������ֲ������ʱ����
	
	public void nextMusic(Intent nextIntent,int buttonName) {
		nextIntent.putExtra("buttonName", buttonName);
		itemOpen=0;
		nextIntent.putExtra("itemOpen", itemOpen);
		ListContent nextSongContent = null;
		if(currentSong == null) {
			nextIntent.putExtra("currentSong", currentSong);
			startService(nextIntent);
			thePlayPosition = 0;
			//�������ֵ��߼�
			initMediaPlayer(nextSongContent,thePlayPosition);
		}else if(currentSong != null && shuffleModel != 1) {
			if(itemPosition == -10) {
				thePlayPosition = buttonPosition++;
			}else {
				thePlayPosition = itemPosition;
				itemPosition++;
			}
			thePlayPosition++;
			if(itemPosition > listContent.size()-1 ) {
				itemPosition = 0;
			}
			/*if(thePlayPosition <0 ) {
				thePlayPosition = listContent.size()-1;
			}*/
			/*nextIntent.putExtra("repeatModel", repeatModel);
			nextIntent.putExtra("itemPosition", itemPosition);
			nextIntent.putExtra("currentSong", currentSong);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("buttonPosition", buttonPosition);*/
			sendState(nextIntent);
			startService(nextIntent);
			buttonPosition++;
		}else if(currentSong != null && shuffleModel == 1) {
			/*nextIntent.putExtra("repeatModel", repeatModel);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("shuffleModel", shuffleModel);
			nextIntent.putExtra("currentSong", currentSong);
			nextIntent.putExtra("buttonPosition", buttonPosition);*/
			sendState(nextIntent);
			startService(nextIntent);
		}
	}
	
	//�������
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
	}
	
	public void viewOnClickListener() {
		previous.setOnClickListener(this);
		repeat.setOnClickListener(this);
		play.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		next.setOnClickListener(this);
	}
	
	//���ݸ�service������
	public void sendState(Intent sendIntent) {
		sendIntent.putExtra("buttonName", buttonName);
		sendIntent.putExtra("repeatModel", repeatModel);
		sendIntent.putExtra("shuffleModel", shuffleModel);
		sendIntent.putExtra("itemPosition", itemPosition);
		sendIntent.putExtra("currentSong", currentSong);
		sendIntent.putExtra("shuffleModel", shuffleModel);
		sendIntent.putExtra("buttonPosition", buttonPosition);
		sendIntent.putExtra("equalSong", "equalSong");//�������ε��ͬһ�׸�
		sendIntent.putExtra("currentSong", currentSong);
		sendIntent.putExtra("nextSong", nextSong);
	}
	
	@Override
	protected void  onDestroy() {
		super.onDestroy();
		Intent stopServiceIntent = new Intent(this,PlayMusicService.class);
		stopService(stopServiceIntent);
		if(mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}
}

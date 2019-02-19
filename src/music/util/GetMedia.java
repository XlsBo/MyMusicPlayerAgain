package music.util;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import music.activity.R;

//��ȡ�ֻ��ڸ����ı��⣬ʱ����
public class GetMedia {
	
	public static List<ListContent> getSongInfo(Context context){
		List<ListContent> songInfos = new ArrayList<ListContent>();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		//cursor.moveToFirst();
		while(cursor.moveToNext()) {
			ListContent songInfo = new ListContent();
			//��ȡ�����ı���
			String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
			//MediaStore.Audio.Media.DURATION ��Ƶ�ļ��ĳ���ʱ�䣬�Ժ���Ϊ��λ
			int songDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//��ø�����·��
			String songArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));		
			int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
			if(isMusic != 0) {
				songInfo.setImageId(R.drawable.first_song);
				songInfo.setSong(songName);
				songInfo.setDuration(songDuration);
				songInfo.setSongPath(songPath);
				songInfo.setSongArtist(songArtist);
				songInfos.add(songInfo);
			}
		}
		cursor.close();
		return songInfos;
	}
	
	//������ת���ɡ��֣��롱�ĸ�ʽ
	public static String formatTime(int time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}
		//trim()����ʵ����trim�����ַ�������Unicode����С�ڵ���32��\u0020���������ַ���
		//substring()�����þ��ǽ�ȡ���ַ�����ĳһ����;�������ǣ���0��ʼ�������ַ�����һ�����Ϊ0��
		return min + ":" + sec.trim().substring(0, 2);
	}

}

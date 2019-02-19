package music.util;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import music.activity.R;

//获取手机内歌曲的标题，时长。
public class GetMedia {
	
	public static List<ListContent> getSongInfo(Context context){
		List<ListContent> songInfos = new ArrayList<ListContent>();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		//cursor.moveToFirst();
		while(cursor.moveToNext()) {
			ListContent songInfo = new ListContent();
			//获取歌曲的标题
			String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
			//MediaStore.Audio.Media.DURATION 音频文件的持续时间，以毫秒为单位
			int songDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//获得歌曲的路径
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
	
	//将毫秒转换成“分：秒”的格式
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
		//trim()方法实际上trim掉了字符串两端Unicode编码小于等于32（\u0020）的所有字符。
		//substring()的作用就是截取父字符串的某一部分;本例中是，从0开始的两个字符（第一个标号为0）
		return min + ":" + sec.trim().substring(0, 2);
	}

}

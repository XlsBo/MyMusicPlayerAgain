package music.util;

//歌曲的相关信息
public class ListContent {
	private int imageId;
	private String song;
	private int duration;
	private String songPath;
	private String songArtist;
	
	public ListContent() {
		super();
	}
	
	
	public ListContent(int imageId,String song,int duration,String songPath,String songArtist) {
		super();
		this.imageId = imageId;
		this.song = song;
		this.duration = duration;
		this.songPath = songPath;
		this.songArtist = songArtist;
	}
	//获得歌曲的图片，歌名，时长，路径
	public int getImageId(){
		return imageId;
	}
	
	public String getSong(){
		return song;
	}
	
	public int getDuration(){
		return duration;
	}
	//设置歌曲的图片，歌名，时长，路径
	public String getSongPath() {
		return songPath;
	}
	public String getSongArtist() {
		return songArtist;
	}
	
	public int setImageId(int ImageId) {
		return this.imageId = ImageId;
	}
	
	public String setSong(String song) {
		return this.song = song;
	}
	
	public int setDuration(int duration) {
		return this.duration = duration;
	}
	
	public String setSongPath(String songPath) {
		return this.songPath = songPath;
	}
	public String setSongArtist(String songArtist) {
		return this.songArtist = songArtist;
	}

}

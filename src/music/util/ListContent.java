package music.util;

//�����������Ϣ
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
	//��ø�����ͼƬ��������ʱ����·��
	public int getImageId(){
		return imageId;
	}
	
	public String getSong(){
		return song;
	}
	
	public int getDuration(){
		return duration;
	}
	//���ø�����ͼƬ��������ʱ����·��
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

package com.gdky005.padservice.dao.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangQing on 16/3/12.
 */
public class KuwoKBean implements Parcelable {
    /**
     * ret : ok
     * total : 746
     * musiclist : [{"yr":"2016-03-11","musicrid":"6964448","name":"一个优质女朋友的基本标准（一路向北Vol.742）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-10","musicrid":"6964609","name":"一个优质男朋友的基本标准(一路向北Vol.741)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-09","musicrid":"6962137","name":"不爱你的人,就别奉陪了(一路向北Vol.740)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-08","musicrid":"6958195","name":"去接近一个充满正能量的人（一路向北Vol.739）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-07","musicrid":"6958194","name":"姑娘，有人爱你的时候别太\u201c作\u201d（一路向北Vol.738）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-06","musicrid":"6953758","name":"异地恋，会好吗？（一路向北Vol.737）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-05","musicrid":"6953634","name":"讨厌自己，是变得更好的开始（一路向北Vol.736）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-04","musicrid":"6953633","name":"爱的时候有多好，分手的时候就有多糟（一路向北Vol.735）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-03","musicrid":"6951202","name":"当你开始想念一个人的时候,你就输了(一路向北Vol.734)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-02","musicrid":"6949430","name":"20几岁的女孩该干嘛？赚钱！(一路向北Vol.733)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-03-01","musicrid":"6948040","name":"幸福会迟到，但它永远不会缺席(一路向北Vol.732)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-29","musicrid":"6942973","name":"我们都过了耳听爱情的年纪(一路向北Vol.731)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-28","musicrid":"6943146","name":"爱他们的时候,我们像条狗(一路向北VoL.730)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-27","musicrid":"6941268","name":"除了努力,你没有第二条备选之路(一路向北Vol.729)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-26","musicrid":"6941780","name":"你无法恋爱的理由是什么？（一路向北VoL.728）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-25","musicrid":"6941270","name":"所有的我爱你，都是不在乎结局的心甘情愿（一路向北VoL.727）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-24","musicrid":"6939243","name":"为什么你总做渣男收割机？（一路向北Vol.726）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-23","musicrid":"6937890","name":"你没有必要活成所有人喜欢的样子(一路向北VoL.725)","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-22","musicrid":"6934284","name":"女人最好只爱八分，付出五分（一路向北VoL.724）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-21","musicrid":"6928936","name":"你还在坚持，小时候的梦想嘛？（一路向北Vol.723）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-20","musicrid":"6928930","name":"你活得光鲜亮丽，父母却在低声下气（一路向北VoL.722）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-19","musicrid":"6928931","name":"是不是一定要结婚？（一路向北Vol.721）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-18","musicrid":"6926967","name":"他不爱你，才舍得暧昧（一路向北Vol.720）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-17","musicrid":"6925106","name":"找男朋友的第一标准是什么？（一路向北VoL.719）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-16","musicrid":"6899940","name":"对不起，我只想过好95%的生活（一路向北Vol.718）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-15","musicrid":"6899952","name":"限量版爱情（一路向北Vol.717）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-14","musicrid":"6899941","name":"多少感情，都被这句话摧毁了（一路向北Vol.716）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-13","musicrid":"6899946","name":"情人节：爱情来了，到底该不该主动呢？（一路向北Vol.715）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"},{"yr":"2016-02-12","musicrid":"6899950","name":"他用余生为我暖一杯茶（下）（一路向北Vol.714）","artist":"小北","album":"一路向北","formats":"WMA96|WMA128|MP3128|MP3192|AAC48"}]
     */

    private String ret;
    private int total;
    private int programId;
    /**
     * yr : 2016-03-11
     * musicrid : 6964448
     * name : 一个优质女朋友的基本标准（一路向北Vol.742）
     * artist : 小北
     * album : 一路向北
     * formats : WMA96|WMA128|MP3128|MP3192|AAC48
     */

    private List<MusiclistEntity> musiclist;

    public void setRet(String ret) {
        this.ret = ret;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setMusiclist(List<MusiclistEntity> musiclist) {
        this.musiclist = musiclist;
    }

    public String getRet() {
        return ret;
    }

    public int getTotal() {
        return total;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public List<MusiclistEntity> getMusiclist() {
        return musiclist;
    }

    public static class MusiclistEntity {
        private String yr;
        private String musicrid;
        private String name;
        private String artist;
        private String album;
        private String formats;

        public void setYr(String yr) {
            this.yr = yr;
        }

        public void setMusicrid(String musicrid) {
            this.musicrid = musicrid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public void setFormats(String formats) {
            this.formats = formats;
        }

        public String getYr() {
            return yr;
        }

        public String getMusicrid() {
            return musicrid;
        }

        public String getName() {
            return name;
        }

        public String getArtist() {
            return artist;
        }

        public String getAlbum() {
            return album;
        }

        public String getFormats() {
            return formats;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ret);
        dest.writeInt(this.total);
        dest.writeInt(this.programId);
        dest.writeList(this.musiclist);
    }

    public KuwoKBean() {
    }

    protected KuwoKBean(Parcel in) {
        this.ret = in.readString();
        this.total = in.readInt();
        this.programId = in.readInt();
        this.musiclist = new ArrayList<MusiclistEntity>();
        in.readList(this.musiclist, List.class.getClassLoader());
    }

    public static final Parcelable.Creator<KuwoKBean> CREATOR = new Parcelable.Creator<KuwoKBean>() {
        public KuwoKBean createFromParcel(Parcel source) {
            return new KuwoKBean(source);
        }

        public KuwoKBean[] newArray(int size) {
            return new KuwoKBean[size];
        }
    };
}

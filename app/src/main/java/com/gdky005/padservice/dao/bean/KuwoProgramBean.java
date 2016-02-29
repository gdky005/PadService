package com.gdky005.padservice.dao.bean;

import java.util.List;

/**
 * Created by WangQing on 16/2/29.
 */
public class KuwoProgramBean {

    /**
     * id : 1013437783
     * info : 全宇宙最温暖最治愈的音乐脱口秀。主播：莫大人、萱草
     * ispub : true
     * musiclist : [{"album":"酷我音乐调频","albumid":"161281","artist":"莫大人&萱草","artistid":"71357","copyright":"0","duration":"1740","formats":"WMA96|WMA128|MP3H|MP3192|MP3128|AAC48|AAC24","hasmv":"0","id":"6941263","is_point":"0","muti_ver":"0","name":"说一次让你难忘的邂逅(酷我音乐调频Vol.206)","online":"1","params":"说一次让你难忘的邂逅(酷我音乐调频Vol.206);莫大人&萱草;酷我音乐调频;552278942;2889440367;MUSIC_6941263;170925567;3122123695;6941263;0;0;MV_0;0","pay":"0","score100":"67"},{"album":"酷我音乐调频","albumid":"161281","artist":"莫大人&萱草","artistid":"71357","copyright":"0","duration":"1730","formats":"WMA96|WMA128|MP3H|MP3192|MP3128|AAC48|AAC24","hasmv":"0","id":"6926964","is_point":"0","muti_ver":"0","name":"分享一次失败的约会经历(酷我音乐调频Vol.205)","online":"1","params":"分享一次失败的约会经历(酷我音乐调频Vol.205);莫大人&萱草;酷我音乐调频;3678424539;792163504;MUSIC_6926964;134583297;1353894652;6926964;0;0;MV_0;0","pay":"0","score100":"62"}]
     * pic : http://img1.kuwo.cn/star/userpl2015/shoujigd/random/3158_auto_150.jpg
     * pn : 0
     * result : ok
     * rn : 2
     * tag : 小清新,80后,90后,温暖,开心
     * title : 酷我音乐调频
     * total : 215
     * type :
     * uid : 165896359
     * uname :
     */

    private int id;
    private String info;
    private boolean ispub;
    private String pic;
    private int pn;
    private String result;
    private int rn;
    private String tag;
    private String title;
    private int total;
    private String type;
    private int uid;
    private String uname;
    /**
     * album : 酷我音乐调频
     * albumid : 161281
     * artist : 莫大人&萱草
     * artistid : 71357
     * copyright : 0
     * duration : 1740
     * formats : WMA96|WMA128|MP3H|MP3192|MP3128|AAC48|AAC24
     * hasmv : 0
     * id : 6941263
     * is_point : 0
     * muti_ver : 0
     * name : 说一次让你难忘的邂逅(酷我音乐调频Vol.206)
     * online : 1
     * params : 说一次让你难忘的邂逅(酷我音乐调频Vol.206);莫大人&萱草;酷我音乐调频;552278942;2889440367;MUSIC_6941263;170925567;3122123695;6941263;0;0;MV_0;0
     * pay : 0
     * score100 : 67
     */

    private List<MusiclistEntity> musiclist;

    public void setId(int id) {
        this.id = id;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setIspub(boolean ispub) {
        this.ispub = ispub;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setPn(int pn) {
        this.pn = pn;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setRn(int rn) {
        this.rn = rn;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setMusiclist(List<MusiclistEntity> musiclist) {
        this.musiclist = musiclist;
    }

    public int getId() {
        return id;
    }

    public String getInfo() {
        return info;
    }

    public boolean isIspub() {
        return ispub;
    }

    public String getPic() {
        return pic;
    }

    public int getPn() {
        return pn;
    }

    public String getResult() {
        return result;
    }

    public int getRn() {
        return rn;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public int getTotal() {
        return total;
    }

    public String getType() {
        return type;
    }

    public int getUid() {
        return uid;
    }

    public String getUname() {
        return uname;
    }

    public List<MusiclistEntity> getMusiclist() {
        return musiclist;
    }

    public static class MusiclistEntity {
        private String album;
        private String albumid;
        private String artist;
        private String artistid;
        private String copyright;
        private String duration;
        private String formats;
        private String hasmv;
        private String id;
        private String is_point;
        private String muti_ver;
        private String name;
        private String online;
        private String params;
        private String pay;
        private String score100;

        public void setAlbum(String album) {
            this.album = album;
        }

        public void setAlbumid(String albumid) {
            this.albumid = albumid;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public void setArtistid(String artistid) {
            this.artistid = artistid;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public void setFormats(String formats) {
            this.formats = formats;
        }

        public void setHasmv(String hasmv) {
            this.hasmv = hasmv;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setIs_point(String is_point) {
            this.is_point = is_point;
        }

        public void setMuti_ver(String muti_ver) {
            this.muti_ver = muti_ver;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setOnline(String online) {
            this.online = online;
        }

        public void setParams(String params) {
            this.params = params;
        }

        public void setPay(String pay) {
            this.pay = pay;
        }

        public void setScore100(String score100) {
            this.score100 = score100;
        }

        public String getAlbum() {
            return album;
        }

        public String getAlbumid() {
            return albumid;
        }

        public String getArtist() {
            return artist;
        }

        public String getArtistid() {
            return artistid;
        }

        public String getCopyright() {
            return copyright;
        }

        public String getDuration() {
            return duration;
        }

        public String getFormats() {
            return formats;
        }

        public String getHasmv() {
            return hasmv;
        }

        public String getId() {
            return id;
        }

        public String getIs_point() {
            return is_point;
        }

        public String getMuti_ver() {
            return muti_ver;
        }

        public String getName() {
            return name;
        }

        public String getOnline() {
            return online;
        }

        public String getParams() {
            return params;
        }

        public String getPay() {
            return pay;
        }

        public String getScore100() {
            return score100;
        }
    }
}

package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Seo on 2016/12/17.
 *
 * this class stores information of each video
 *
 * note;
 * this class is serializable, but child classes extending this may be not.
 * when passed with Intent, it is safe to convert them to the instance of this class
 */


public class VideoInfo implements Serializable{

    //index of list, not content
    protected int index = 0;

    //only in case of ranking
    protected String genre;
    protected String rankKind;
    protected String period;
    protected String pubDate;

    //common
    protected String title;
    protected String id;
    protected String date;
    protected String description;
    protected String[] thumbnailUrl;
    protected int length = -1;

    //statistics of the video
    protected int viewCounter = -1;
    protected int commentCounter = -1;
    protected int myListCounter = -1;

    //tags attributed to the video
    protected String[] tags;

    protected float point = 0f;

    public static final int GENRE = 0;
    public static final int RANK_KIND = 1;
    public static final int PERIOD = 2;
    public static final int PUB_DATE = 3;
    public static final int TITLE = 4;
    public static final int ID = 5;
    public static final int DATE = 6;
    public static final int DESCRIPTION = 7;
    public static final int THUMBNAIL_URL = 8;
    public static final int LENGTH = 9;
    public static final int VIEW_COUNTER = 10;
    public static final int COMMENT_COUNTER = 11;
    public static final int MY_LIST_COUNTER = 12;
    public static final int TAGS = 13;
    public static final int TAG = 14;

    public VideoInfo(int index){
        this.index = index;
    }
    public VideoInfo(){
        index = 0;
    }
    public int getIndex(){
        return index;
    }
    public void setString(int key, String value){
        if ( getString(key) != null ){
            return;
        }
        switch ( key ){
            case GENRE:
                genre = value;
                break;
            case RANK_KIND:
                rankKind = value;
                break;
            case PERIOD:
                period = value;
                break;
            case PUB_DATE:
                pubDate = value;
                break;
            case TITLE:
                title = value;
                break;
            case ID:
                id = value;
                break;
            case DATE:
                date = value;
                break;
            case DESCRIPTION:
                description = value;
                break;
            default:
        }
    }
    public String getString(int key){
        switch ( key ){
            case GENRE:
                return genre;
            case RANK_KIND:
                return rankKind;
            case PERIOD:
                return period;
            case PUB_DATE:
                return pubDate;
            case TITLE:
                return title;
            case ID:
                return id;
            case DATE:
                return date;
            case DESCRIPTION:
                return description;
            default:
                return null;
        }
    }
    public int getInt(int key){
        switch ( key ){
            case LENGTH:
                return length;
            case VIEW_COUNTER:
                return viewCounter;
            case COMMENT_COUNTER:
                return commentCounter;
            case MY_LIST_COUNTER:
                return myListCounter;
            default:
                return 0;
        }
    }
    public void setInt(int key, int value){
        switch ( key ){
            case LENGTH:
                length = value;
                break;
            case VIEW_COUNTER:
                viewCounter = value;
                break;
            case COMMENT_COUNTER:
                commentCounter = value;
                break;
            case MY_LIST_COUNTER:
                myListCounter = value;
                break;
            default:
        }
    }
    public void setTags(String[] tags){
        if ( this.tags == null ){
            this.tags = tags;
        }
    }
    public void setTags(ArrayList<String> tags){
        if ( tags != null && tags.size() > 0 ){
            String[] array = new String[tags.size()];
            for ( int i=0 ; i<array.length ; i++){
                array[i] = tags.get(i);
            }
            setTags(array);
        }
    }
    public String[] getTags(){
        return tags;
    }
    public void setThumbnailUrl (String[] thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }
    public void setThumbnailUrl(ArrayList<String> urls){
        if ( urls != null && urls.size() > 0 ){
            String[] array = new String[urls.size()];
            for ( int i=0 ; i<array.length ; i++){
                array[i] = urls.get(i);
            }
            setThumbnailUrl(array);
        }
    }
    public String getThumbnailUrl (boolean isHigh){
        if ( thumbnailUrl == null ){
            return null;
        }
        if ( isHigh ){
            return thumbnailUrl[thumbnailUrl.length-1];
        }else{
            return  thumbnailUrl[0];
        }
    }
    public String getThumbnailUrl (){
        return getThumbnailUrl(false);
    }
    public void setPoint(float point){
        this.point = point;
    }
    public float getPoint(){
        return point;
    }

    //safe down cast
    public VideoInfoManager downCast(){
        if (  this instanceof VideoInfo ) {
            VideoInfoManager info = new VideoInfoManager(index);
            info.setString(VideoInfo.GENRE, genre);
            info.setString(VideoInfo.RANK_KIND, rankKind);
            info.setString(VideoInfo.PERIOD, period);
            info.setString(VideoInfo.PUB_DATE, pubDate);
            info.setString(VideoInfo.TITLE, title);
            info.setString(VideoInfo.ID, id);
            info.setString(VideoInfo.DATE, date);
            info.setString(VideoInfo.DESCRIPTION, description);
            info.setThumbnailUrl(thumbnailUrl);
            info.setInt(VideoInfo.LENGTH, length);
            info.setInt(VideoInfo.VIEW_COUNTER, viewCounter);
            info.setInt(VideoInfo.COMMENT_COUNTER, commentCounter);
            info.setInt(VideoInfo.MY_LIST_COUNTER, myListCounter);
            info.setTags(tags);
            return info;
        }
        return (VideoInfoManager)this;
    }

}


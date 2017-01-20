package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.SharedPreferences;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Seo on 2016/12/10.
 * this class stores setting data
 *
 * reference;
 * how to serialize and pass object to other activities : http://techbooster.jpn.org/andriod/application/7190/
 *
 * note;
 * when passed with Intent, SharedPreferences can not passed together
 * so procedure with SharedPreferences is defined in parent class.
 * when setting value, be sure not to forget to call initialize() with SharedPreferences
 */

public class InfoStore extends InfoManager implements Serializable {

    protected int loginTarget;
    protected boolean isAutoLogin;
    protected boolean isYouTubeWatch;
    protected boolean isYouTubeComment;
    protected boolean isRecommend;
    protected int userID = 0;
    protected String userName;
    protected int videoChoice;
    public static final int VIDEO_NOTHING = 0;
    public static final int VIDEO_OTHER = 1;
    public static final int VIDEO_SEARCH = 2;


    private int cookieNum;
    private int[] cookieVersion;
    private String[] cookieName;
    private String[] cookieValue;
    private String[] cookiePath;
    private String[] cookieDomain;

    private VideoInfo videoInfo;
    private boolean login = false;

    //key for intent
    public static final String INTENT_SETTING = "setting";
    public static final String INTENT_MENU = "menu";
    public static final String INTENT_RANKING = "ranking";
    public static final String INTENT_MYLIST = "myList";
    public static final String INTENT_SEARCH = "search";
    public static final String INTENT_YOUTUBE = "youTube";

    protected InfoStore(SharedPreferences data){
        initialize(data);
        loginTarget = loadLoginTarget();
        isAutoLogin = loadIsAutoLogin();
        isYouTubeWatch = loadIsYouTubeWatch();
        isYouTubeComment = loadIsYouTubeComment();
        isRecommend = loadIsRecommend();
        videoChoice = loadVideoChoice();
    }

    //setters and getters
    public CookieStore getCookieStore(){
            CookieStore cookieStore = new DefaultHttpClient().getCookieStore();
            for (int i = 0; i < cookieNum; i++) {
                BasicClientCookie cookie = new BasicClientCookie(cookieName[i], cookieValue[i]);
                cookie.setVersion(cookieVersion[i]);
                cookie.setPath(cookiePath[i]);
                cookie.setDomain(cookieDomain[i]);
                cookieStore.addCookie(cookie);
            }
        return cookieStore;
    }
    public void setCookieStore(CookieStore cookieStore){
        if ( cookieStore == null ){
            login = false;
            cookieNum = 0;
            cookieVersion = new int[0];
            cookieName = new String[0];
            cookieValue = new String[0];
            cookieDomain = new String[0];
            cookiePath = new String[0];
            userName = "";
            userID = 0;
            return;
        }
        List<Cookie> list = cookieStore.getCookies();
        cookieNum = list.size();
        cookieVersion = new int[cookieNum];
        cookieName = new String[cookieNum];
        cookieValue = new String[cookieNum];
        cookieDomain = new String[cookieNum];
        cookiePath = new String[cookieNum];
        for ( int i=0 ; i<cookieNum ; i++){
            Cookie cookie = list.get(i);
            cookieVersion[i] = cookie.getVersion();
            cookieName[i] = cookie.getName();
            cookieValue[i] = cookie.getValue();
            cookiePath[i] = cookie.getPath();
            cookieDomain[i] = cookie.getDomain();
        }
        if ( cookieNum > 1 ){
            login = true;
        }
    }
    public VideoInfoManager getVideo(){
        if ( videoInfo instanceof VideoInfo ){
            videoInfo = videoInfo.downCast();
        }
        return (VideoInfoManager)videoInfo;
    }
    public void setVideo(VideoInfo videoInfo){
        if ( ! (videoInfo instanceof VideoInfo) ){
            videoInfo = ((VideoInfoManager)videoInfo).upCast();
        }
        this.videoInfo = videoInfo;
    }
    public boolean isLogin(){
        return login;
    }
    public String getMail (int target){
        return loadMail(target);
    }
    public String getMail (){
        return loadMail(loginTarget);
    }
    public String getPass (){
        return loadPass(loginTarget);
    }
    public String getPass (int target){
        return loadMail(target);
    }
    public void setAutoLogin(boolean isAutoLogin){
        this.isAutoLogin = isAutoLogin;
        saveIsAutoLogin(isAutoLogin);
    }
    public void setYouTubeWatch(boolean isYouTubeWatch){
        this.isYouTubeWatch = isYouTubeWatch;
        saveIsYouTubeWatch(isYouTubeWatch);
    }
    public void setYouTubeComment(boolean isYouTubeComment){
        this.isYouTubeComment = isYouTubeComment;
        saveIsYouTubeComment(isYouTubeComment);
    }
    public void setRecommend(boolean isRecommend){
        this.isRecommend = isRecommend;
        saveIsRecommend(isRecommend);
    }
    public void setVideoChoice(int videoChoice){
        this.videoChoice = videoChoice;
        saveVideoChoice(videoChoice);
    }
    public int getLoginTarget (){
        return loginTarget;
    }
    public void setLoginTarget(int target){
        if ( target >= 0 && target < num ){
            loginTarget = target;
        }
    }
    public boolean isAutoLogin(){
        return isAutoLogin;
    }
    public boolean isYouTubeWatch(){
        return isYouTubeWatch;
    }
    public boolean isYouTubeComment(){
        return isYouTubeComment;
    }
    public boolean isRecommend(){
        return isRecommend;
    }
    public int getUserID(){
        return userID;
    }
    public String getUserName(){
        return userName;
    }
    public void setUserID (int userID){
        this.userID = userID;
    }
    public void setUserName (String userName){
        if ( userName != null ) {
            this.userName = userName;
        }
    }
    public int getVideoChoice(){
        return videoChoice;
    }
}

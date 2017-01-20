package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.SharedPreferences;

/**
 * Created by Seo on 2017/01/15.
 *
 * this class save and load setting date in SharedPreferences
 *
 * note;
 * before save and load, you have to pass SharedPreferences by calling initialize()
 *
 */

public class InfoManager  {

    private SharedPreferences.Editor editor;
    private SharedPreferences data;

    private boolean initialized = false;

    //key for sharedPreference
    private final String LOGIN_KEY = "login";
    protected final String AUTO_LOGIN_KEY = "autoLogin";
    protected final String VIDEO_CHOICE_KEY = "videoChoice";
    protected final String YOUTUBE_WATCH_KEY = "youTubeWatch";
    protected final String YOUTUBE_COMMENT_KEY = "youTubeComment";
    protected final String NICO_RECOMMEND = "recommend";
    private String[] mailKey;
    private String[] passKey;
    protected int num = 5;

    public void initialize (SharedPreferences data){
        if ( data != null ) {
            this.data = data;
            this.editor = data.edit();
            mailKey = new String[num];
            passKey = new String[num];
            for (int i = 0; i < num; i++) {
                mailKey[i] = "mail-" + i;
                passKey[i] = "pass-" + i;
            }
            initialized = true;
        }
    }

    final public int getNum(){
        return num;
    }
    final protected int loadLoginTarget(){
        if ( initialized ) {
            return data.getInt(LOGIN_KEY, 0);
        }else{
            return  0;
        }
    }
    final protected boolean loadIsAutoLogin(){
        if ( initialized ) {
            return data.getBoolean(AUTO_LOGIN_KEY, true);
        }else{
            return true;
        }
    }
    final protected boolean loadIsYouTubeWatch(){
        if ( initialized ) {
            return data.getBoolean(YOUTUBE_WATCH_KEY, true);
        }else{
            return true;
        }
    }
    final protected boolean loadIsYouTubeComment(){
        if ( initialized ) {
            return data.getBoolean(YOUTUBE_COMMENT_KEY, true);
        }else{
            return true;
        }
    }
    final protected boolean loadIsRecommend(){
        if ( initialized ) {
            return data.getBoolean(NICO_RECOMMEND, true);
        }else{
            return true;
        }
    }
    final protected int loadVideoChoice(){
        if ( initialized ) {
            return data.getInt(VIDEO_CHOICE_KEY, InfoStore.VIDEO_OTHER);
        }else{
            return InfoStore.VIDEO_OTHER;
        }
    }
    final protected String loadMail(int target){
        if ( initialized && target >= 0 && target < num ){
            return data.getString(mailKey[target],"");
        }else{
            return "";
        }
    }
    final protected String loadPass(int target){
        if ( initialized && target >=0 && target < num ){
            return data.getString(passKey[target],"");
        }else{
            return "";
        }
    }
    final public void save(){
        if ( initialized ){
            editor.apply();
        }
    }
    final protected void saveLoginTarget(int target){
        if ( initialized && target >= 0 && target < num ){
            editor.putInt(LOGIN_KEY,target);
        }
    }
    final protected void saveIsAutoLogin(boolean target){
        if ( initialized ){
            editor.putBoolean(AUTO_LOGIN_KEY,target);
        }
    }
    final protected void saveIsYouTubeWatch(boolean target){
        if ( initialized ){
            editor.putBoolean(YOUTUBE_WATCH_KEY,target);
        }
    }
    final protected void saveIsYouTubeComment(boolean target){
        if ( initialized ){
            editor.putBoolean(YOUTUBE_COMMENT_KEY,target);
        }
    }
    final protected void saveVideoChoice(int target){
        if ( initialized ){
            editor.putInt(VIDEO_CHOICE_KEY,target);
        }
    }
    final protected void saveMail(int target, String mail){
        if ( initialized && target >= 0 && target < num ){
            editor.putString(mailKey[target],mail);
        }
    }
    final protected void savePass(int target, String pass){
        if ( initialized && target >= 0 && target < num ){
            editor.putString(passKey[target],pass);
        }
    }
    final protected void saveIsRecommend(boolean target){
        if ( initialized ){
            editor.putBoolean(NICO_RECOMMEND,target);
        }
    }


}

package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;


import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo on 2017/01/01.
 *
 * this class keeps information of each comment
 * also provides methods to parse response in Json to this class
 *
 * reference
 * algorithm of merge sort : 東京大学出版会　情報科学入門 ISBN978-4-13-062452-7
 * how to get comment : https://blog.hayu.io/web/nicovideo-comment-api
 *
 *
 * note
 * Json passed to parse() is supposed to gotten from message server like this;
 * http://msg.nicovideo.jp/10/api.json/thread?version=20090904&thread={0}&res_from=-{1}
 * url of message server can be gotten from getflv API
 * query should be like that (details is known)
 *      params of query;
 *      {0} : thread ID of target video, also can be gotten from getflv API
 *      {1} : max number of comment, must not be over 1000
 */

public class CommentInfo {

    public long start;
    public String[] mail;   //list of comment commands, not mail address
    public String content;  //comment
    public String date;
    public int anonymity = 1;   //1:normal

    private boolean initialized = false;

    public float x;
    public float y;
    public float length = -1f;
    public float speed;
    public int color = -1;
    public float size = -1f;
    public int position = -1;

    public static final int POSITION_UP = 0;
    public static final int POSITION_MIDDLE = 1;
    public static final int POSITION_BOTTOM = 2;

    private static final float SIZE_MEDIUM = 0.9f;
    private static final float SIZE_SMALL = 0.8f;
    private static final float SIZE_BIG = 1f;

    private final static Map<String,Integer> colorMap = new HashMap<String,Integer>(){
        {
            put("white", 0xffffffff);
            put("red",0xffff0000);
            put("pink",0xffff8080);
            put("orange",0xffffcc00);
            put("yellow",0xffffff00);
            put("green",0xff00ff00);
            put("cyan",0xff00ffff);
            put("blue",0xff0000ff);
            put("purple",0xffc000ff);
            put("black",0xff000000);
        }
    };
    private final static Map<String,Integer> positionMap= new HashMap<String,Integer>(){
        {
            put("naka",POSITION_MIDDLE);
            put("ue",POSITION_UP);
            put("shita",POSITION_BOTTOM);
        }
    };
    private final static Map<String,Float> sizeMap = new HashMap<String,Float>(){
        {
            put("medium",SIZE_MEDIUM);
            put("small",SIZE_SMALL);
            put("big",SIZE_BIG);
        }
    };

    //in constructor pass Json relevant to each comment,
    //then Json is parsed and fields are initialized
    public CommentInfo (JSONObject item){
        initialize(item);
    }

    //set command of comment from value of "mail"
    private void setCommands(){
        //set default value
        color = colorMap.get("white");
        position = POSITION_MIDDLE;
        size = SIZE_MEDIUM;
        if ( mail != null ) {
            for (String key : mail) {
                if ( colorMap.containsKey(key)) {
                    color = colorMap.get(key);
                } else if ( positionMap.containsKey(key)) {
                    position = positionMap.get(key);
                } else if ( sizeMap.containsKey(key)) {
                    size = sizeMap.get(key);
                }
            }
        }
    }
    private void initialize (JSONObject item){
        try {
            //value of "vpos" seems to be time when comment appear,
            // but unit is decimal sec, not milli sec
            int vpos = item.getInt("vpos");
            start = (long) vpos * 10;
            if (item.has("anonymity")) {
                anonymity = item.getInt("anonymity");
            }
            date = convertDate(item.getLong("date"));
            if (item.has("mail")) {
                mail = item.getString("mail").split("\\s");
            }
            content = item.getString("content");
            setCommands();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private String convertDate (long time){
        return VideoInfoManager.dateFormatBase.format(new Date(time * 1000));
    }

    //initialize fields needed for being shown on Canvas
    //when you get Paint from Canvas, call this with the Paint
    public void initialize(float width, Paint paint, float span, float offset){
        if ( ! initialized ){
            length = paint.measureText(content);
            speed = (width + length )/span;
            y = offset;
            switch ( position ){
                case POSITION_BOTTOM:
                case POSITION_UP:
                    x = (width-length)/2f;
                    break;
                case POSITION_MIDDLE:
                    x = width;
                    break;
            }
        }
    }

    //parse Json and return list of CommentInfo
    public static List<CommentInfo> parse(JSONArray root){
        List<CommentInfo>commentList = new ArrayList<CommentInfo>();
        try{
            for ( int i=0 ; i<root.length() ; i++){
                JSONObject item = root.getJSONObject(i);
                if ( item.has("chat")) {
                    item = item.getJSONObject("chat");
                    if ( !item.has("vpos") || !item.has("content") ){
                        continue;
                    }
                    commentList.add( new CommentInfo(item));
                }
            }
            return sortComment(commentList);
        } catch( JSONException e){
            e.printStackTrace();
        }
        return null;
    }
    public static List<CommentInfo> parse(String res){
        try{
            JSONArray root = new JSONArray(res);
            return parse(root);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    //sort list of CommentInfo along the time series, using merge sort
    private static List<CommentInfo> sortComment(List<CommentInfo> list){
        List<List<CommentInfo>> sort = new ArrayList<List<CommentInfo>>();
        for ( int i=0 ; i<list.size() ; i++){
            List<CommentInfo> item = new ArrayList<CommentInfo>();
            item.add(list.get(i));
            sort.add(item);
        }
        int n = sort.size();
        while ( n > 1 ){
            List<List<CommentInfo>> temp = new ArrayList<List<CommentInfo>>();
            for ( int i=0 ; i<n/2 ; i++){
                temp.add(merge(sort.get(2*i),sort.get(2*i+1)));
            }
            if ( sort.size() == 2*n+1){
                temp.add(sort.get(2*n));
            }
            sort = temp;
            n = sort.size();
        }
        return sort.get(0);
    }

    private static List<CommentInfo> merge (List<CommentInfo> a, List<CommentInfo> b){
        List<CommentInfo> list = new ArrayList<CommentInfo>();
        while ( a.size() > 0 && b.size() > 0 ){
            if ( a.get(0).start < b.get(0).start ){
                list.add(a.get(0));
                a.remove(0);
            }else{
                list.add(b.get(0));
                b.remove(0);
            }
        }
        for ( int i=0 ; i<a.size() ; i++){
            list.add(a.get(i));
        }
        for ( int i=0 ; i<b.size() ; i++){
            list.add(b.get(i));
        }
        return list;
    }

}

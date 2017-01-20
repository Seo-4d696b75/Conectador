package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo on 2017/01/15.
 *
 * this class extending VideoInfoManager provides methods to parse JSON
 * from https://www.googleapis.com/youtube/v3/search?type=video&part=snippet&q={keyword}&key={APIkey}
 * but, this JSON do not contain all the fields, then get them in JSON
 * from https://www.googleapis.com/youtube/v3/videos?part={target_fields}&id={VideoID}&key={APIkey}
 *
 * reference;
 * usage of API : https://developers.google.com/youtube/v3/docs/videos?hl=ja
 *                  http://so-zou.jp/web-app/tech/web-api/google/youtube/data-api/v3/
 */

public class YouTubeVideoInfo extends VideoInfoManager {

    public YouTubeVideoInfo (int index, JSONObject item){
        super(index);
        initialize(item);
    }
    public YouTubeVideoInfo(JSONObject item){
        initialize(item);
    }

    private void initialize(JSONObject item){
        try {
            id = item.getJSONObject("id").getString("videoId");
            JSONObject snippet = item.getJSONObject("snippet");
            date = convertDate( snippet.getString("publishedAt"));
            title = snippet.getString("title");
        }catch (JSONException e){
            Log.d("search_youtube","fail to parse Json");
        }
    }

    public boolean setDetail(JSONObject item){
        try{
            JSONObject snippet = item.getJSONObject("snippet");
            description = snippet.getString("description");
            JSONObject thumbnails = snippet.getJSONObject("thumbnails");
            ArrayList<String> urlList = new ArrayList<String>();
            for (Iterator<String> keys = thumbnails.keys(); keys.hasNext() ;){
                JSONObject thumbnail = thumbnails.getJSONObject(keys.next());
                urlList.add(thumbnail.getString("url"));
            }
            setThumbnailUrl(urlList);
            JSONObject detail = item.getJSONObject("contentDetails");
            JSONObject statistics = item.getJSONObject("statistics");
            length = parseLength(detail.getString("duration"));
            viewCounter = getInt(statistics,"viewCount");
            commentCounter = getInt(statistics,"commentCount");
            getInt(statistics,"likeCount");
            getInt(statistics,"favoriteCount");
            //info.setInt(VideoInfo.MY_LIST_COUNTER,0);
            if ( snippet.has("tags")){
                JSONArray tags = snippet.getJSONArray("tags");
                this.tags = new String[tags.length()];
                for ( int k=0 ; k<tags.length() ; k++ ){
                    this.tags[k] = tags.get(k).toString();
                }
            }
            return true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }

    private String convertDate (String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormatBase.format(dateFormat.parse(date));
        }catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    private int parseLength (String target){
        Matcher matcher = Pattern.compile("PT([0-9]*)M([0-9]+)S").matcher(target);
        if ( matcher.find() ){
            try{
                int min = Integer.parseInt(matcher.group(1));
                int sec = Integer.parseInt(matcher.group(2));
                return 60*min+sec;
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    private int getInt(JSONObject object, String key){
        if ( object.has(key)){
            try {
                return object.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static List<VideoInfo> parse (String response){
        try {
            JSONObject result = new JSONObject(response);
            JSONArray items = result.getJSONArray("items");
            List<VideoInfo> list = new ArrayList<VideoInfo>();
            for ( int i=0 ; i<items.length() ; i++){
                JSONObject item = items.getJSONObject(i);
                list.add(new YouTubeVideoInfo(i,item));
            }
            return list;
        }catch(JSONException e){
            Log.d("search_youtube","fail to parse Json");
            return null;
        }
    }

    public static boolean setDetail(List<VideoInfo> list, String response){
        try {
            JSONObject result = new JSONObject(response);
            JSONArray items = result.getJSONArray("items");
            if (items.length() != list.size()) {
                return false;
            }
            for ( int i=0 ; i<items.length() ; i++) {
                if ( !((YouTubeVideoInfo)list.get(i)).setDetail(items.getJSONObject(i)) ){
                    return false;
                }
            }
            return true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }
}

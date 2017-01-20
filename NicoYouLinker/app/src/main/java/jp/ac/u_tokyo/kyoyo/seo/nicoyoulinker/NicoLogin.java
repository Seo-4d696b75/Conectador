package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;



import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seo on 2016/12/10.
 *
 * this class try to login NicoNico and get userID, userName and userIconImage in background
 *
 * references :
 * how to login with Apache : https://teratail.com/questions/31972
 *                              http://c-loft.com/blog/?p=1196
 * regular expression : java.keicode.com/lang/regexp-split.php
 * get image via http : http://logicalerror.seesaa.net/article/419965567.html
 * how to get user name : http://7cc.hatenadiary.jp/entry/nico-user-id-to-name
 */

public class NicoLogin {

    private int statusCode = -1;
    private String res = null;
    private int user = 0;

    public NicoLogin(){}

    public CookieStore tryLogin(String url, String mail, String pass){
        if ( mail == null || pass == null ){
            return  null;
        }
        try {
            HttpPost httpPost = new HttpPost(new URI(url));
            DefaultHttpClient client = new DefaultHttpClient();

            ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("mail_tel", mail));
            parameters.add(new BasicNameValuePair("password", pass));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters,"UTF-8"));

            HttpResponse httpResponse = client.execute(httpPost);
            /* レスポンスコードの取得（Success:200、Auth Error:403、Not Found:404、Internal Server Error:500）*/
            statusCode = httpResponse.getStatusLine().getStatusCode();
            CookieStore cookieStore = null;
            if (statusCode == 200) {
                cookieStore = client.getCookieStore();
                res = EntityUtils.toString(httpResponse.getEntity());
            }
            client.getConnectionManager().shutdown();
            if ( cookieStore != null && isSuccess(cookieStore) ){
                return cookieStore;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public String getUserName(String url){
        if ( res == null || user <= 0 ){
            return "";
        }
        Matcher matcher = Pattern.compile("<span id=\"siteHeaderUserNickNameContainer\">(.+?)</span>").matcher(res);
        if ( matcher.find() ){
            return matcher.group(1);
        }
        try {
            HttpGet httpGet = new HttpGet(url);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpGet);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                res = EntityUtils.toString(httpResponse.getEntity());
                matcher = Pattern.compile("<nickname>(.+?)</nickname>").matcher(res);
                if ( matcher.find() ){
                    String target = matcher.group(1);
                    return target;
                }
            }
            client.getConnectionManager().shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isSuccess(CookieStore cookieStore) {
        List<Cookie> list = new ArrayList<Cookie>();
        list = cookieStore.getCookies();
        for ( Cookie cookie : list ){
            Log.d("login-cookies",cookie.getName() +" :"  +cookie.getValue());
        }
        if ( list.size() > 1 ){
            return true;
        }
        return false;
    }

    public int getUserID(){
        if ( res == null ){
            Log.d("userID","no http response");
            return -1;
        }
        Matcher matcher = Pattern.compile("var User = \\{ id: ([0-9]+), age: ([0-9]+), isPremium: (false|true), isOver18: (false|true), isMan: (false|true) \\};").matcher(res);
        if ( matcher.find() ){
            user = Integer.parseInt(matcher.group(1));
            String age = matcher.group(2);
            //following params also can be gotten, but not used in this app
            boolean isPremium = Boolean.valueOf(matcher.group(3));
            boolean isOver18 = Boolean.valueOf(matcher.group(4));
            boolean isMan = Boolean.valueOf(matcher.group(5));
        }else{
            user = 0;
        }
        return user;
    }

    public Drawable getUserIcon(String path){
        Drawable image = null;
        try {
            URL url = new URL(path);
            InputStream input = (InputStream)url.getContent();
            image = Drawable.createFromStream(input,"uer_icon");
            input.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return image;
    }



}

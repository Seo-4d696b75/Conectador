package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Seo on 2016/12/17.
 *
 * this class defines the logic of http communication
 *
 * reference;
 * important point of URL encoder : http://weblabo.oscasierra.net/java-urlencode/
 * usage of regular expression : http://nobuo-create.net/seikihyougen/#i-13
 *
 */

public class HttpResponseGetter {

    public String getResponse(String url){
        return getResponse(url,null);
    }

    public String getResponse(String path, CookieStore cookieStore){
        try{
            //some characters cause IllegalArgumentException
            path = replaceMetaSymbol(path);
            HttpGet httpGet = new HttpGet(path);
            DefaultHttpClient client = new DefaultHttpClient();
            if ( cookieStore != null){
                client.setCookieStore(cookieStore);
            }
            httpGet.setHeader("Connection", "keep-Alive");
            HttpResponse httpResponse = client.execute(httpGet);
            String res = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            client.getConnectionManager().shutdown();
            if ( statusCode == 200 ) {
                return  res;
            }
        }catch ( Exception e){
            return  null;
        }
        return null;
    }

    private final Map<String,String> symbolMap = new HashMap<String, String>(){
        {
            put("\\+","%2b");
            put("\\s","%20");
            put("\"","%22");
            put("\\|","%7c");
        }
    };
    private String replaceMetaSymbol(String str){
        for ( String key : symbolMap.keySet() ){
            str = str.replaceAll(key,symbolMap.get(key));
        }
        return str;
    }
}

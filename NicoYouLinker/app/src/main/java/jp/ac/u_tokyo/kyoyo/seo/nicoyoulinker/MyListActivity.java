package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo on 2016/12/15.
 *
 * References;
 * usage of API? for getting myList and its group : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
 * usage of Matcher.class : http://www.javadrive.jp/regex/repeat/index1.html
 *
 * note;
 * getting my list needs login session
 *
 *
 */

public class MyListActivity extends CustomListActivity {


    private Map<String,String> myListGroup;
    private List<VideoInfo> list;

    private Spinner spinnerMyList;
    private ListView listViewMyList;

    private final int REQUEST_MYLIST_GROUP = 0;
    private final int REQUEST_MYLIST = 1;
    private final int REQUEST_TEMP_MYLIST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylist);

        res = getResources();

        setTitle(res.getString(R.string.mylist_title));

        receiveIntent(getIntent());

        spinnerMyList = (Spinner)findViewById(R.id.spinnerMyListSelect);
        listViewMyList = (ListView)findViewById(R.id.listViewMyList);

        //get myList group ans show if any
        new BackGround(REQUEST_MYLIST_GROUP).execute();

    }

    protected void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore)intent.getSerializableExtra(InfoStore.INTENT_MYLIST);
            if ( info != null ){
                receive = true;
                this.info = info;
            }
        }
        if ( !receive ){
            Log.d("myList","failed to receive intent");
            finish();
        }
    }

    private void setMyListSpinner(){
        final String mes = res.getString(R.string.mylist_select_mes);
        final String temp = res.getString(R.string.mylist_temp);
        List<String> list = new ArrayList<String>();
        list.add(mes);
        for ( String name : myListGroup.keySet() ){
            list.add(name);
        }
        list.add(temp);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MyListActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                list
        );
        spinnerMyList.setAdapter(adapter);
        spinnerMyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getSelectedItem();
                if ( name.equals(temp)){
                    new BackGround(REQUEST_TEMP_MYLIST).execute();
                }else if( myListGroup.containsKey(name) ){
                    new BackGround(REQUEST_MYLIST,myListGroup.get(name)).execute();
                }else{
                    Toast.makeText(MyListActivity.this,mes,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MyListActivity.this,mes,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class BackGround extends AsyncTask<String, Void, String> {

        /**
         * this class gets myList group and myList
         *
         * Caution;
         * getting myList and its group needs login session stored in cookie
         * myList group returns in Json format, while myList returns in XML
         *
         * References;
         * usage of API? for getting myList and its group : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
         * usage of Matcher.class : http://www.javadrive.jp/regex/repeat/index1.html
         */

        private ProgressDialog progress = null;
        private int request = -1;
        private final String SUCCESS = "success";
        private String target;

        public BackGround(int request){
            if ( request == REQUEST_MYLIST_GROUP || request == REQUEST_TEMP_MYLIST ){
                this.request = request;
            }
        }
        public BackGround(int request, String target){
            this.target = target;
            if ( request == REQUEST_MYLIST ){
                this.request = request;
            }
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MyListActivity.this);
            switch (request){
                case REQUEST_MYLIST_GROUP:
                    progress.setMessage(res.getString(R.string.mylist_group_progress));
                    break;
                case REQUEST_MYLIST:
                    progress.setMessage(res.getString(R.string.mylist_list_progress));
                    break;
                case REQUEST_TEMP_MYLIST:
                    progress.setMessage(res.getString(R.string.mylist_default_progress));
                    break;
                default:
                    return;
            }
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String ... params) {
            String response,path;
            switch (request) {
                case REQUEST_MYLIST_GROUP:
                    path = res.getString(R.string.mylist_group_url);
                    response = new HttpResponseGetter().getResponse(path,info.getCookieStore());
                    if ( response == null ){
                        Log.d("myListGroup","fail to get response");
                        return null;
                    }
                    try{
                        JSONObject json = new JSONObject(response);
                        if ( !json.optString("status").equals("ok")){
                            Log.d("myListGroup","invalid access");
                            return null;
                        }
                        JSONArray array = json.optJSONArray("mylistgroup");
                        myListGroup = new HashMap<String,String>();
                        for ( int i=0 ; i<array.length() ; i++){
                            JSONObject item = array.optJSONObject(i);
                            myListGroup.put(item.optString("name"),item.optString("id"));
                        }
                    }catch (JSONException e){
                        return null;
                    }
                    if ( myListGroup != null ){
                        return SUCCESS;
                    }
                    break;
                case REQUEST_MYLIST:
                    if ( target == null ){
                        break;
                    }
                    path = String.format(res.getString(R.string.mylist_url),target);
                    response = new HttpResponseGetter().getResponse(path,info.getCookieStore());
                    if ( response == null ){
                        Log.d("myList","fail to get response");
                        break;
                    }
                    list = null;
                    list =  RankingVideoInfo.parse(response,null,null,null);
                    if ( list != null ){
                        return SUCCESS;
                    }
                    break;
                case REQUEST_TEMP_MYLIST:
                    path = res.getString(R.string.mylist_temp_rul);
                    response = new HttpResponseGetter().getResponse(path,info.getCookieStore());
                    if ( response == null ){
                        break;
                    }
                    list = null;
                    list = TempMyListVideoInfo.parse(response);
                    if ( list != null ){
                        return SUCCESS;
                    }
                    break;
                default:
            }
            return "fail";
        }

        @Override
        protected void onPostExecute(String status) {
            if ( progress != null ){
                progress.cancel();
                progress = null;
            }
            switch (request){
                case REQUEST_MYLIST_GROUP:
                    if ( status != null && status.equals(SUCCESS) ){
                        setMyListSpinner();
                    }else{
                        Toast.makeText(MyListActivity.this, res.getString(R.string.mylist_failure_mes), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_MYLIST:
                case REQUEST_TEMP_MYLIST:
                    if ( status != null && status.equals(SUCCESS) ){
                        listViewMyList.setAdapter(new RankingListAdapter(MyListActivity.this, list, res));
                        listViewMyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                RankingListAdapter adapter = (RankingListAdapter)parent.getAdapter();
                                VideoInfo info = adapter.getItem(position);
                                onListItemClicked(info);
                            }
                        });
                    }else{
                        Toast.makeText(MyListActivity.this, res.getString(R.string.mylist_failure_mes), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
            }

        }
    }


}

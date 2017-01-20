package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.List;

/**
 * Created by Seo on 2017/01/14.
 *
 * this class provides procedure when item(VideoInfo) of ListView is selected,
 * which is common in several activities
 * and this abstract class has to be extended to instantiate
 *
 * reference;
 * how to get recommend : http://d.hatena.ne.jp/picas/20080202/1201955339
 *
 */

public abstract class CustomListActivity extends AppCompatActivity {

    protected Resources res;
    protected InfoStore info;

    private AlertDialog dialog;
    private VideoInfo videoInfo;
    private List<VideoInfo> recommendList;
    private boolean listPrepared, contextPrepared, recommendInitialize;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        recommendInitialize = false;
    }

    @Override
    protected void onStart(){
        super.onStart();
        contextPrepared = true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        contextPrepared = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        contextPrepared = true;
        showRecommend();
    }

    abstract protected void receiveIntent(Intent intent);

    protected void onListItemClicked(VideoInfo videoInfo){
        if ( info == null || videoInfo == null ){
            return;
        }
        switch ( info.getVideoChoice() ){
            case InfoStore.VIDEO_OTHER:     //play in outside player
                String path = res.getString(R.string.watch_rul) + videoInfo.getString(VideoInfo.ID);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                startActivity(intent);
                break;
            case InfoStore.VIDEO_SEARCH:    // search in YouTube
                info.setVideo(videoInfo);
                intent = new Intent(CustomListActivity.this, YouTubeActivity.class);
                intent.putExtra(InfoStore.INTENT_YOUTUBE,info);
                startActivity(intent);
                break;
            default:
                break;
        }
        if ( info.isRecommend() ) {
            this.videoInfo = videoInfo;
            recommendInitialize = true;
            new RecommendGetter().execute();
        }
    }

    protected final void showRecommend(){
        if ( listPrepared && contextPrepared && recommendInitialize ){
            recommendInitialize = false;
            if ( dialog != null ){
                dialog.cancel();
                dialog = null;
            }
            Context context = CustomListActivity.this;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(res.getString(R.string.recommend_title));
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup root = (ViewGroup)findViewById(R.id.dialogRecommendRoot);
            View view = inflater.inflate(R.layout.dialog_recommend, root, true);
            ListView listView = (ListView)view.findViewById(R.id.listViewRecommend);
            listView.setAdapter(new RankingListAdapter(context, recommendList, res, true));
            listView.setOnItemClickListener( new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RankingListAdapter adapter = (RankingListAdapter)parent.getAdapter();
                    VideoInfo info = adapter.getItem(position);
                    onListItemClicked(info);
                }
            });
            builder.setView(view);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog = null;
                }
            });
            dialog = builder.create();
            dialog.show();
        }
    }

    //if needed, search and show recommended videos
    private class RecommendGetter extends AsyncTask<String, Void, String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            listPrepared = false;
            if ( info.getVideoChoice() == InfoStore.VIDEO_NOTHING ){
                progress = new ProgressDialog(CustomListActivity.this);
                progress.setMessage(res.getString(R.string.recommend_progress));
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String path = res.getString(R.string.recommend_url) + videoInfo.getString(VideoInfo.ID);
            String response = new HttpResponseGetter().getResponse(path);
            if (response != null) {
                recommendList = RecommendVideoInfo.parse(response);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (progress != null) {
                progress.cancel();
                progress = null;
            }
            if ( recommendList != null ){
                listPrepared = true;
                showRecommend();
            }

        }
    }
}

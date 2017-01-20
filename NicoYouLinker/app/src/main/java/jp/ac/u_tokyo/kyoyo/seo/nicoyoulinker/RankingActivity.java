package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Seo on 2016/12/11.
 *
 * this activity extending CustomListActivity searches ranking and show
 *
 * References;
 * usage of API getting ranking : https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
 * list of niconico category : http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0
 *
 */

public class RankingActivity extends CustomListActivity {

    private Button buttonRanking;
    private TextView textViewMes;
    private ListView listViewRanking;

    private List<VideoInfo> list;

    private int genre = 0;
    private int kind = 0;
    private int period = 0;

    private String[] genreArray;
    private String[] kindArray;
    private String[] periodArray;
    private String[] genreArrayName;
    private String[] kindArrayName;
    private String[] periodArrayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        res = getResources();
        setTitle(res.getString(R.string.ranking_title));


        receiveIntent(getIntent());

        buttonRanking  = (Button)findViewById(R.id.buttonRanking);
        textViewMes = (TextView)findViewById(R.id.textViewRankingMes);
        listViewRanking = (ListView)findViewById(R.id.listViewRanking);

        buttonRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRankingSetDialog();
            }
        });

        setStringArray();
        showRankingSetDialog();

    }

    protected void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore)intent.getSerializableExtra(InfoStore.INTENT_RANKING);
            if ( info != null ){
                receive = true;
                this.info = info;
            }
        }
        if ( !receive ){
            Log.d("ranking","failed to receive intent");
            finish();
        }
    }

    private void setStringArray(){
        if ( res == null ){
            return;
        }
        //list of params needed for getting ranking
        genreArray = res.getStringArray(R.array.spinner_array_genre);
        kindArray = res.getStringArray(R.array.spinner_array_kind);
        periodArray = res.getStringArray(R.array.spinner_array_period);
        genreArrayName = res.getStringArray(R.array.spinner_array_genre_name);
        kindArrayName = res.getStringArray(R.array.spinner_array_kind_name);
        periodArrayName = res.getStringArray(R.array.spinner_array_period_name);
    }

    private void showRankingSetDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RankingActivity.this);
        builder.setTitle(res.getString(R.string.dialog_ranking_title));
        Context context = RankingActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogRankingRoot);
        View v = inflater.inflate(R.layout.dialog_set_ranking, root, true);
        final Spinner spinnerGenre = (Spinner)v.findViewById(R.id.spinnerRankingGenre);
        final Spinner spinnerKind = (Spinner)v.findViewById(R.id.spinnerRankingKind);
        final Spinner spinnerPeriod = (Spinner)v.findViewById(R.id.spinnerRankingPeriod);
        spinnerGenre.setSelection(genre);
        spinnerKind.setSelection(kind);
        spinnerPeriod.setSelection(period);
        builder.setView(v);
        builder.setMessage(res.getString(R.string.dialog_ranking_mes));
        builder.setPositiveButton(res.getString(R.string.search_go), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(MenuActivity.this, genre+"/"+kind+"/"+period,Toast.LENGTH_SHORT).show();
                genre = spinnerGenre.getSelectedItemPosition();
                kind = spinnerKind.getSelectedItemPosition();
                period = spinnerPeriod.getSelectedItemPosition();
                new BackGround().execute();
            }
        });
        builder.create();
        builder.show();
    }


    private class BackGround extends AsyncTask<String, Void, String> {

        /**
         * this class gets ranking
         *
         * Caution;
         * getting ranking needs login session
         *
         * References;
         * usage of API getting ranking :
         *      https://ja.osdn.net/projects/nicolib/wiki/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E8%A7%A3%E6%9E%90%E3%83%A1%E3%83%A2
         * list of niconico category : http://dic.nicovideo.jp/a/%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%82%BF%E3%82%B0
         *
         */

        private ProgressDialog progress = null;
        private boolean success = false;
        private final String SUCCESS = "success";

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(RankingActivity.this);
            progress.setMessage(res.getString(R.string.ranking_progress));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
            textViewMes.setText(String.format(res.getString(R.string.ranking_mes),
                    genreArrayName[genre],
                    kindArrayName[kind],
                    periodArrayName[period]));
        }

        @Override
        protected String doInBackground(String... params) {
            list = null;
            String url = res.getString(R.string.ranking_url);
            String rankKindPara = kindArray[kind];
            String genrePara = genreArray[genre];
            String periodPara = periodArray[period];
            String path = String.format(url,rankKindPara,periodPara,genrePara);
            String response = new HttpResponseGetter().getResponse(path,info.getCookieStore());
            if ( response == null ){
                return "fail";
            }
            list = RankingVideoInfo.parse(response,genrePara,periodPara,rankKindPara);
            if (list != null) {
                return SUCCESS;
            }
            return "fail";
        }

        @Override
        protected void onPostExecute(String status) {
            progress.cancel();
            if ( status != null && status.equals(SUCCESS) ) {
                listViewRanking.setAdapter(new RankingListAdapter(RankingActivity.this, list, res));
                listViewRanking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RankingListAdapter adapter = (RankingListAdapter)parent.getAdapter();
                        VideoInfo info = adapter.getItem(position);
                        onListItemClicked(info);
                    }
                });
            } else {
                Toast.makeText(RankingActivity.this, res.getString(R.string.ranking_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

}

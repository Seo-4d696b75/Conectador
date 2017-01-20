package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;


/**
 * Created by Seo on 2016/12/28.
 *
 * this activity search YouTube for video which is most close to target video from niconico
 * if need, play YouTube video and show comments from nionico
 *
 * note;
 * API key is needed to use YouTube Data API v3
 *
 * references;
 * usage of youtubeView : http://developers.google.com/youtube/android/player/
 * dp pixel : http://inujirushi123.blog.fc2.com/blog-entry-105.html
 * usage of Canvas : https://tech.recruit-mp.co.jp/mobile/remember_canvas1/
 * how to depict in SurfaceView : http://blog.oukasoft.com/%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%A0/%E3%80%90android%E3%80%91surfaceview%E3%82%92%E4%BD%BF%E3%81%A3%E3%81%A6%E3%82%B2%E3%83%BC%E3%83%A0%E3%81%A3%E3%81%BD%E3%81%84%E3%82%A2%E3%83%97%E3%83%AA%E3%82%92%E4%BD%9C%E3%81%A3%E3%81%A6%E3%81%BF/
 * usage of YouTube Data API v3  :
 *              https://developers.google.com/youtube/v3/docs/videos?hl=ja
 *              http://so-zou.jp/web-app/tech/web-api/google/youtube/data-api/v3/
 * usage of iterator : http://kenzy-goldentime.blogspot.jp/2011/07/android-jsonobjectjsonarray.html
 * list of Em punctuations : http://junk-blog.com/font_list/#no8
 * how to get comment : https://blog.hayu.io/web/nicovideo-comment-api
 */

public class YouTubeActivity extends YouTubeFailureRecoveryActivity {

    private static final String APIkey = "AIzaSyAPCO71TmOTvvdEHRpRICquoMGh88_THNU";

    private Resources res;
    private InfoStore info;
    private VideoInfoManager videoInfo;
    private List<VideoInfo> list;
    private List<CommentInfo> commentList;
    private float commentMax = 2f;  //param for calculate request comment number
    private int commentNum = 12;    //number of comment row
    private float span = 3.5f;      //span in which comment flows from one side to the other

    private YouTubePlayerView youTubeView;
    private YouTubePlayer player;
    private String ID;
    private TextView textTitle,textDescription;
    private ImageView thumb;
    private CommentSurfaceView surfaceView;
    private Thread thread;
    private AlertDialog resultDialog;

    private int width;  //pixel
    private int height; //pixel

    private boolean isAttached = false;

    public static String getAPIkey(){
        return APIkey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        receiveIntent(getIntent());
        res = getResources();

        if ( info.isYouTubeWatch() ){
            DisplayMetrics metrics = res.getDisplayMetrics();
            width = metrics.widthPixels;
            height = (int)(width * 9.0 / 16.0);

            textTitle = (TextView)findViewById(R.id.textViewYoutubeWatchTitle);
            textDescription = (TextView)findViewById(R.id.textViewYoutubeWatchDescription);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
            ViewGroup root = (ViewGroup)findViewById(R.id.linearLayoutYoutubeViewContainer);
            youTubeView = new YouTubePlayerView(this);
            root.addView(youTubeView,params);
            if ( info.isYouTubeComment()){
                //if comments from Nico are shown
                thumb = (ImageView)findViewById(R.id.imageViewYoutubeWatchThumb);
                surfaceView = new CommentSurfaceView(this);
                root = (ViewGroup)findViewById(R.id.frameLayoutYoutubeRoot);
                root.addView(surfaceView,params);
                surfaceView.getHolder().addCallback(new CommentCallBack());
                params = thumb.getLayoutParams();
                params.width = width;
                params.height = height;
                thumb.setLayoutParams(params);
                textTitle.setTextColor(res.getColor(R.color.hint));
                thumb.setImageDrawable(res.getDrawable(R.drawable.temp_thumbnail));
            }
        }
        new BackGround(res.getString(R.string.youtube_search_url),res.getString(R.string.youtube_resource_url)).execute();


    }

    //this callback is set in SurfaceView, in which comments are depicted
    public class CommentCallBack implements SurfaceHolder.Callback,Runnable{

        private SurfaceHolder holder;
        private long interval;
        private long timer1,timer2;
        private long time,preTime;
        private Queue<CommentInfo> queue;
        private Canvas canvas;
        private Paint paint;
        private List<CommentHolder> holderList;
        private int index;
        private float gap;
        private float offset;

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            isAttached = true;
            this.holder = holder;
            //preparation of run()
            timer1 = System.currentTimeMillis();
            thread = new Thread(this);
            queue = new ArrayDeque<CommentInfo>();
            index = 0;
            preTime = 0;
            gap = (float)height/commentNum;
            paint = new Paint();
            paint.setTextSize(gap);
            paint.setShadowLayer(gap/10f,gap/20f,gap/20f,Color.BLACK);
            offset = -paint.getFontMetrics().ascent;
            holderList = new ArrayList<CommentHolder>(){
                {
                    add( new CommentHolder(offset,CommentInfo.POSITION_UP));
                    add( new CommentHolder(offset+gap*(commentNum-1),CommentInfo.POSITION_BOTTOM));
                    for ( int i=1 ; i<commentNum-1 ; i++){
                        add( new CommentHolder(offset+gap*i,CommentInfo.POSITION_MIDDLE));
                    }
                }
            };
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isAttached = false;
            thread = null;
            this.holder = null;
        }

        @Override
        public void run(){
            while ( isAttached ){
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                if ( player == null ){
                    time = 0;
                }else{
                    time = player.getCurrentTimeMillis();
                }
                //detect back
                if ( preTime > time ){
                    for ( int i=0 ; i<commentList.size() ; i++){
                        if ( (long)commentList.get(i).start >= time ){
                            index = i;
                            break;
                        }
                    }
                }
                preTime = time;
                //add CommentInfo to queue
                for ( int i=index ; i<commentList.size() ; i++){
                    CommentInfo info = commentList.get(i);
                    if ( (long)info.start < time ){
                        queue.offer(info);
                        index++;
                    }else{
                        break;
                    }
                }
                timer2 = System.currentTimeMillis();
                interval = timer2 - timer1;
                timer1 = timer2;
                for ( CommentHolder holder : holderList){
                    if ( holder.upDate(queue.peek()) ){
                        queue.remove();
                    }
                }
                holder.unlockCanvasAndPost(canvas);

            }
        }

        //this class keeps and manages CommentInfo in each row on SurfaceView
        private class CommentHolder {

            public int position;        //param of position, refer to CommentInfo
            public List<CommentInfo> commentList;
            public float offset;        //offset of y-coordinate on SurfaceView in pixels
            private float rate = 0.3f;  //how much ratio of width should be kept between comments in same row
            private boolean canAdd;     //can add next comment?

            //in constructor, set offset and position
            public CommentHolder (float offset, int position){
                commentList = new ArrayList<CommentInfo>();
                this.offset = offset;
                this.position = position;
            }

            //called to update comment
            public boolean upDate(CommentInfo next){
                canAdd = true;
                Iterator<CommentInfo> iterator = commentList.iterator();
                while ( iterator.hasNext() ){
                    CommentInfo info = iterator.next();
                    switch ( position ){
                        case CommentInfo.POSITION_UP:
                        case CommentInfo.POSITION_BOTTOM:
                            if ( info.start + span * 1000 < time ){
                                iterator.remove();
                            }else{
                                paint.setColor(info.color);
                                paint.setTextSize(gap*info.size);
                                canvas.drawText(info.content,info.x,info.y,paint);
                                canAdd = false;
                            }
                            break;
                        case CommentInfo.POSITION_MIDDLE:
                            info.x -= info.speed*interval/1000;
                            paint.setColor(info.color);
                            paint.setTextSize(gap*info.size);
                            canvas.drawText(info.content,info.x,info.y,paint);
                            if ( info.x <= -info.length){
                                iterator.remove();
                            }
                            if ( canAdd && info.x + info.length > width * (1-rate) ){
                                canAdd = false;
                            }
                            break;
                    }
                }
                if ( canAdd ){
                    if ( next == null || this.position != next.position ){
                        return false;
                    }
                    next.initialize(width,paint,span,offset);commentList.add(next);
                    Log.d("comment",String.format("show comment > 0x%06x:%s",Integer.valueOf(next.color),next.content));
                    return true;
                }
                return false;
            }
        }
    }

    private void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore) intent.getSerializableExtra(InfoStore.INTENT_YOUTUBE);
            if ( info != null){
                receive = true;
                this.info = info;
                videoInfo = (VideoInfoManager)info.getVideo();
            }
        }
        if ( !receive ){
            Log.d("youTube","failed to receive intent");
            finish();
        }
    }

    private void showResultDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(YouTubeActivity.this);
        builder.setTitle(res.getString(R.string.youtube_search_mes));
        Context context = YouTubeActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogYoutubeRoot);
        View view = inflater.inflate(R.layout.dialog_youtube_search, root, true);
        ImageView nicoThumbnail = (ImageView)view.findViewById(R.id.imageViewNicoThumbnail);
        TextView nicoLength = (TextView)view.findViewById(R.id.textViewNicoLength);
        TextView nicoTitle = (TextView)view.findViewById(R.id.textViewNicoTitle);
        TextView nicoViewCounter = (TextView)view.findViewById(R.id.textViewNicoView);
        TextView nicoMyListCounter = (TextView)view.findViewById(R.id.textViewNicoMyList);
        ListView resultList = (ListView)view.findViewById(R.id.listViewYoutube);
        resultList.setAdapter(new RankingListAdapter(YouTubeActivity.this, list, res, true));
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RankingListAdapter adapter = (RankingListAdapter)parent.getAdapter();
                VideoInfoManager result = (VideoInfoManager)adapter.getItem(position);
                if ( info.isYouTubeWatch() ){
                    if ( info.isYouTubeComment() ){
                        new BackGround(result).execute();
                        new ThumbnailGetter(result,thumb,true).execute();
                    }else{
                        watchYoutube(result);
                    }
                    resultDialog.cancel();
                    resultDialog = null;
                }else{
                    String path = res.getString(R.string.youtube_warch_url) + result.getString(VideoInfo.ID);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    startActivity(intent);
                    finish();
                }
            }
        });
        new ThumbnailGetter(videoInfo,nicoThumbnail,false).execute();
        nicoLength.setText(((VideoInfoManager)videoInfo).formatLength());
        nicoTitle.setText(videoInfo.getString(VideoInfo.TITLE));
        String viewCounter = String.format(res.getString(R.string.ranking_viewCounter_format),
                ((VideoInfoManager)videoInfo).formatCounter(VideoInfo.VIEW_COUNTER) );
        nicoViewCounter.setText(viewCounter);
        String myListCounter = String.format(res.getString(R.string.ranking_mylilstCounter_format),
                ((VideoInfoManager)videoInfo).formatCounter(VideoInfo.MY_LIST_COUNTER) );
        nicoMyListCounter.setText(myListCounter);
        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        resultDialog = builder.create();
        resultDialog.show();
    }

    private void watchYoutube(VideoInfo result){
        ID = result.getString(VideoInfo.ID);
        textTitle.setText(result.getString(VideoInfo.TITLE));
        textDescription.setText(result.getString(VideoInfo.DESCRIPTION));
        youTubeView.initialize(APIkey, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(ID);
            this.player = player;
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    private class BackGround extends AsyncTask<String, Void, String> {

        /**
         * this class searches videos in youTube, gets comment from Nico
         *
         * reference;
         * usage of YouTube Data API v3  :
         *              https://developers.google.com/youtube/v3/docs/videos?hl=ja
         *              http://so-zou.jp/web-app/tech/web-api/google/youtube/data-api/v3/
         * usage of iterator : http://kenzy-goldentime.blogspot.jp/2011/07/android-jsonobjectjsonarray.html
         * list of Em punctuations : http://junk-blog.com/font_list/#no8
         */

        private ProgressDialog progress = null;
        private String searchURL;
        private String resourceURL;
        private String request;
        private VideoInfo result;
        private String[] keyWords;
        private final String SUCCESS = "success";

        private final String SEARCH = "search";
        private final String WATCH = "watch";

        public BackGround(String searchURL, String resourceURL){
            this.searchURL = searchURL;
            this.resourceURL = resourceURL;
            request = SEARCH;
        }
        public BackGround(VideoInfo result){
            request = WATCH;
            this.result = result;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(YouTubeActivity.this);
            progress.setMessage(res.getString(R.string.youtube_progress));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            switch ( request ){
                case SEARCH:
                    //you have to put your own API key
                    if ( APIkey == null ){
                        return null;
                    }
                    //get keyword
                    keyWords = ((VideoInfoManager)videoInfo).splitTitle();
                    StringBuilder builder = new StringBuilder();
                    for ( String item : keyWords ){
                        if ( item.isEmpty() ){
                            continue;
                        }
                        if ( builder.length() > 0 ){
                            builder.append(" ");
                        }
                        builder.append(item);
                    }
                    String keyWord = builder.toString();
                    String path = String.format(searchURL,keyWord,APIkey);
                    String response = new HttpResponseGetter().getResponse(path);
                    if ( response == null ){
                        return null;
                    }
                    list = YouTubeVideoInfo.parse(response);
                    if ( list == null ){
                        return null;
                    }
                    //get details of found videos
                    builder = new StringBuilder();
                    for ( VideoInfo item : list){
                        if ( builder.length() > 0  ){
                            builder.append(",");
                        }
                        builder.append(item.getString(VideoInfo.ID));
                    }
                    path = String.format(resourceURL,builder.toString(),APIkey);
                    response = new HttpResponseGetter().getResponse(path);
                    if ( response == null ){
                        return null;
                    }
                    if ( YouTubeVideoInfo.setDetail(list,response) ){
                        //which is the most close to target video from niconico?
                        if ( analyze() ){
                            sortList();
                            return SUCCESS;
                        }
                    }
                    break;
                case WATCH:
                    int max = (int)((float)videoInfo.getInt(VideoInfo.LENGTH) / span * commentNum * commentMax);
                    if ( videoInfo == null ){
                        return null;
                    }
                    String threadId = "";
                    String ms = "";
                    path = "http://flapi.nicovideo.jp/api/getflv/" + videoInfo.getString(VideoInfo.ID);
                    response = new HttpResponseGetter().getResponse(path,info.getCookieStore());
                    String[] token = response.split("&");
                    for ( int i=0 ; i<token.length ; i++){
                        String[] pairs = token[i].split("=");
                        if ( pairs.length == 2 ) {
                            switch ( pairs[0] ){
                                case "thread_id":
                                    threadId = URLDecoder.decode(pairs[1]);
                                    break;
                                case "ms":
                                    ms = URLDecoder.decode(pairs[1]);
                                    break;
                                default:
                            }
                        }
                    }
                    if ( max > 1000 ){
                        max = 1000;
                    }
                    path = ms.substring(0,ms.length()-1) + ".json/thread?version=20090904&thread=" + threadId + "&res_from=-" + max;
                    response = new HttpResponseGetter().getResponse(path);
                    commentList = CommentInfo.parse(response);
                    if ( commentList != null && commentList.size() > 1 ){
                        return SUCCESS;
                    }
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String status) {
            if (progress != null) {
                progress.cancel();
                progress = null;
            }
            if ( status != null && status.equals(SUCCESS) ) {
                switch (request) {
                    case SEARCH:
                        for (int i = 0; i < list.size(); i++) {
                            VideoInfo info = list.get(i);
                            Log.d("list", String.format("%.4f/%4d/%s/%S",
                                    info.getPoint(),
                                    info.getInt(VideoInfo.LENGTH),
                                    info.getString(VideoInfo.DATE),
                                    info.getString(VideoInfo.TITLE)
                            ));
                        }
                        showResultDialog();
                        break;
                    case WATCH:
                        watchYoutube(result);
                        thread.start();
                        break;
                }
            }
        }

        private boolean analyze(){
            if ( videoInfo.getTags() == null ){
                if ( !videoInfo.complete() ){
                    return false;
                }
            }
            for ( VideoInfo info : list){
                ((VideoInfoManager)info).analyze(videoInfo,keyWords);
            }
            return true;
        }

        private void sortList () {
            boolean fin = false;
            for (int i = list.size() - 1; i > 0; i--) {
                fin = true;
                for (int k = 0; k < i; k++) {
                    if (list.get(k).getPoint() < list.get(k + 1).getPoint()) {
                        fin = false;
                        VideoInfo info = list.get(k);
                        list.set(k, list.get(k + 1));
                        list.set(k + 1, info);
                    }
                }
                if (fin) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ( keyCode == KeyEvent.KEYCODE_BACK){
            isAttached = false;
            if ( resultDialog != null ){
                resultDialog.cancel();
                resultDialog = null;
            }
            finish();
            return true;
        }
        return false;
    }

    private class ThumbnailGetter extends AsyncTask<String, Void, String> {

        private Drawable image;
        private VideoInfoManager item;
        private ImageView thumb;
        private boolean isHigh;

        ThumbnailGetter(VideoInfoManager item, ImageView thumb, boolean isHigh){
            this.item = item;
            this.thumb = thumb;
            this.isHigh = isHigh;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... params) {
            try {
                String path = item.getThumbnailUrl(isHigh);
                if ( path == null){
                    if ( !item.complete() ){
                        return null;
                    }
                    path = item.getThumbnailUrl(isHigh);
                }
                URL url = new URL(path);
                InputStream input = (InputStream)url.getContent();
                image = Drawable.createFromStream(input,"thumbnail");
                input.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if ( image != null ){
                thumb.setImageDrawable(image);
            }
        }
    }
}

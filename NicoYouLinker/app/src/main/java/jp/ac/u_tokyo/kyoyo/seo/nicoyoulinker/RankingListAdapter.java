package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Seo on 2016/12/12.
 *
 * this adapter class adapts VideoInfo.class(and its child class) for ListView
 * also gets thumbnail and its url.
 * should be passed in ListView.setAdapter()
 *
 * References;
 * get image via http : http://logicalerror.seesaa.net/article/419965567.html
 * usage of API for getting video thumbnail : http://dic.nicovideo.jp/a/%E3%83%8B%E3%82%B3%E3%83%8B%E3%82%B3%E5%8B%95%E7%94%BBapi
 *
 * note;
 * this class is based on the lecture material: MessageAdapter.java in UTdroid_ChatApp-master
 * this adapter is not only for ranking
 */

public class RankingListAdapter extends ArrayAdapter<VideoInfo> {

    private LayoutInflater inflater;
    private Resources res;
    private Drawable temp;
    private Map<String,Drawable> thumbNailMap;
    private boolean dialog;

    private Queue<ThumbnailGetter> queue = new ArrayDeque<ThumbnailGetter>();

    public RankingListAdapter(Context context, List<VideoInfo> list, Resources res, boolean dialog) {
        super(context, 0, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.res = res;
        if ( res != null ){
            this.temp = res.getDrawable(R.drawable.temp_thumbnail);
        }
        thumbNailMap = new HashMap<String, Drawable>();
        this.dialog = dialog;
    }
    public RankingListAdapter(Context context, List<VideoInfo> list, Resources res) {
        this(context,list,res,false);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.cell_ranking, null);
        }
        VideoInfoManager item = (VideoInfoManager)this.getItem(position);
        if (item != null) {
            TextView title = (TextView)view.findViewById(R.id.textViewRankingTitle);
            TextView viewCount = (TextView)view.findViewById(R.id.textViewRankingView);
            TextView mylistCount = (TextView)view.findViewById(R.id.textViewMylistView);
            ImageView thumbnail = (ImageView)view.findViewById(R.id.imageViewRankingThumbnail);
            TextView length = (TextView)view.findViewById(R.id.textViewRankingLength);
            title.setText(item.getString(VideoInfo.TITLE));
            if ( dialog ){
                //small text size in dialog
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX,res.getDimension(R.dimen.youtubeListTitle));
            }
            String text = String.format(res.getString(R.string.ranking_viewCounter_format),item.formatCounter(VideoInfo.VIEW_COUNTER));
            viewCount.setText(text);
            text = item.formatLength();
            length.setText(text);
            int counter = item.getInt(VideoInfo.MY_LIST_COUNTER);
            if ( counter > 0 ){
                //Nico
                text = String.format(res.getString(R.string.ranking_mylilstCounter_format), item.formatCounter(VideoInfo.MY_LIST_COUNTER));
            }else{
                //Youtube
                text = "";
            }
            mylistCount.setText(text);
            Drawable image = getImage(item.getString(VideoInfo.ID));
            if ( image == null ){
                thumbnail.setImageDrawable(temp);
                ThumbnailGetter getter = new ThumbnailGetter(item,thumbnail);
                queue.add(getter);
                if ( queue.size() == 1 ){
                    getter.execute();
                }
            }else{
                thumbnail.setImageDrawable(image);
            }

        }

        return view;
    }

    private Drawable getImage(String key){
        if ( thumbNailMap.containsKey(key) ){
            return thumbNailMap.get(key);
        }
        return null;
    }

    private class ThumbnailGetter extends AsyncTask<String, Void, String> {

        private Drawable image;
        private VideoInfoManager item;
        private ImageView thumb;

        ThumbnailGetter(VideoInfoManager item, ImageView thumb){
            this.item = item;
            this.thumb = thumb;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... params) {
            try {
                String path = item.getThumbnailUrl();
                if ( path == null){
                    if ( !item.complete() ){
                        return null;
                    }
                    path = item.getThumbnailUrl();
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
                thumbNailMap.put(item.getString(VideoInfo.ID),image);
            }
            if ( queue.element() != ThumbnailGetter.this ){
                Log.d("getter","queue error");
            }
            queue.remove();
            if ( queue.size() > 0 ){
                queue.element().execute();
            }
        }
    }
}

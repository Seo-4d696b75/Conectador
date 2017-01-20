package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.SurfaceView;

/**
 * Created by Seo on 2017/01/01.
 *
 * this is custom view extending SurfaceView that can be put over another view
 *
 * reference;
 * http://qiita.com/circularuins/items/a61c5e7149f355a54a8b
 * http://blog.oukasoft.com/%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%A0/%E3%80%90android%E3%80%91surfaceview%E3%82%92%E4%BD%BF%E3%81%A3%E3%81%A6%E3%82%B2%E3%83%BC%E3%83%A0%E3%81%A3%E3%81%BD%E3%81%84%E3%82%A2%E3%83%97%E3%83%AA%E3%82%92%E4%BD%9C%E3%81%A3%E3%81%A6%E3%81%BF/
 * https://tech.recruit-mp.co.jp/mobile/remember_canvas1/
 */

public class CommentSurfaceView extends SurfaceView{
    public CommentSurfaceView(Context context){
        super(context);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

}
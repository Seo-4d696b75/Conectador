package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.app.ProgressDialog;

import org.apache.http.client.CookieStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Seo on 2016/12/10.
 *
 * this activity is for top menu
 *
 * reference;
 * how to convert Drawable to Byte[] : http://stackoverflow.com/questions/4435806/drawable-to-byte
 * how to save and load Drawable in local storage : http://stackoverflow.com/questions/8407336/how-to-pass-drawable-between-activities
 */

public class MenuActivity extends AppCompatActivity {

    private Resources res;

    private InfoStore info;

    private Drawable userIcon;

    private SharedPreferences data;

    private String mail;
    private String pass;

    private final int REQUEST_LOGIN = 0;
    private final int REQUEST_USER_ICON = 2;

    private Button buttonSetting;
    private Button buttonGetRanking;
    private Button buttonGetMyList;
    private Button buttonSearch;
    private TextView textViewLoginStatus;
    private TextView textViewUserName;
    private ImageView imageViewUserIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        res = getResources();
        data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_menu);
        buttonSetting = (Button)findViewById(R.id.buttonSetting);
        buttonGetRanking = (Button)findViewById(R.id.buttonGetRank);
        buttonGetMyList = (Button)findViewById(R.id.buttonGetMyList);
        buttonSearch = (Button)findViewById(R.id.buttonSearch);
        textViewLoginStatus = (TextView)findViewById(R.id.textViewLoginStatus);
        textViewUserName = (TextView)findViewById(R.id.textViewUsrName);
        imageViewUserIcon = (ImageView)findViewById(R.id.imageViewUser);

        receiveIntent(getIntent());

        setTitle(res.getString(R.string.app_name));

        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetting();
            }
        });
        buttonGetRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRanking();
            }
        });
        buttonGetMyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyList();
            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_setting:
                openSetting();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore)intent.getSerializableExtra(InfoStore.INTENT_MENU);
            if ( info != null ){
                receive = true;
                this.info = info;
                this.info.initialize(data);
                if ( this.info.getUserID() > 0 ){
                    textViewLoginStatus.setText(res.getString(R.string.login_status_in));
                    textViewUserName.setText(this.info.getUserName());
                    setUserIcon();
                }
                if ( this.info.isAutoLogin() && !this.info.isLogin() ){
                    autoLogin();
                }
            }
        }
        if ( !receive ){
            info = new InfoStore(data);
            if ( info.isAutoLogin() ){
                autoLogin();
            }

        }
    }


    private void autoLogin(){
        mail = info.getMail();
        pass = info.getPass();
        if ( mail.isEmpty() || pass.isEmpty() ){
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            builder.setTitle(res.getString(R.string.edit_account_info));
            Context context = MenuActivity.this;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup root = (ViewGroup)findViewById(R.id.dialogAddRoot);
            final View v = inflater.inflate(R.layout.dialog_add_account, root, true);
            builder.setView(v);
            builder.setMessage(res.getString(R.string.no_account_alert));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText editMail = (EditText) v.findViewById(R.id.editTextMail);
                    EditText editPass = (EditText) v.findViewById(R.id.editTextPass);
                    int target = info.getLoginTarget();
                    info.saveMail(target,editMail.getText().toString());
                    info.savePass(target,editPass.getText().toString());
                    info.save();
                    autoLogin();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MenuActivity.this,res.getString(R.string.no_account_alert),Toast.LENGTH_SHORT).show();
                }
            });
            builder.create();
            builder.show();
        }else{
            new BackGround(REQUEST_LOGIN).execute();
        }
    }


    private class BackGround extends AsyncTask<String, Void, String> {

        private ProgressDialog progress = null;
        private int request = -1;

        public BackGround(int request){
            this.request = request;
        }

        @Override
        protected void onPreExecute() {
            switch (request){
                case REQUEST_LOGIN:
                    progress = new ProgressDialog(MenuActivity.this);
                    progress.setMessage(res.getString(R.string.try_to_login));
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.show();
                    break;
                case REQUEST_USER_ICON:
                    break;
            }

        }

        @Override
        protected String doInBackground(String ... params){
            switch (request){
                case REQUEST_LOGIN:
                    String url = res.getString(R.string.login_url) + res.getString(R.string.my_page_url);
                    NicoLogin nicoLogin = new NicoLogin();
                    CookieStore cookieStore = nicoLogin.tryLogin(url,mail,pass);
                    mail = null;
                    pass = null;
                    info.setCookieStore(cookieStore);
                    if ( info.isLogin() ) {
                        info.setUserID(nicoLogin.getUserID());
                        String nameUrl = res.getString(R.string.user_nickname_url);
                        nameUrl += info.getUserID();
                        info.setUserName(nicoLogin.getUserName(nameUrl));
                    }
                    break;
                case REQUEST_USER_ICON:
                    userIcon = null;
                    int id = info.getUserID();
                    String path = String.format("%s/%d/%d.jpg",res.getString(R.string.user_icon_url),id/10000,id);
                    userIcon = new NicoLogin().getUserIcon(path);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if ( progress != null ){
                progress.cancel();
                progress = null;
            }
            String mes = "";
            switch (request){
                case REQUEST_LOGIN:
                    if ( info.isLogin() ){
                        mes = res.getString(R.string.login_success);
                        textViewLoginStatus.setText(res.getString(R.string.login_status_in));
                        new BackGround(REQUEST_USER_ICON).execute();
                    }else{
                        mes = res.getString(R.string.login_fail);
                    }
                    Toast.makeText(MenuActivity.this,mes,Toast.LENGTH_SHORT).show();
                    String name= info.getUserName();
                    if ( name != null && !name.isEmpty() ){
                        textViewUserName.setText(name);
                        setUserIcon();
                    }
                    break;
                case REQUEST_USER_ICON:
                    if ( userIcon != null ){
                        imageViewUserIcon.setImageDrawable( userIcon);
                        String fileName = String.format(res.getString(R.string.user_icon_path_format),info.getUserID());
                        try {
                            Bitmap bitmap = ((BitmapDrawable)userIcon).getBitmap();
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            byte[] imageByte = outputStream.toByteArray();
                            FileOutputStream fileOutStream = openFileOutput(fileName, MODE_PRIVATE);
                            fileOutStream.write(imageByte);
                            fileOutStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

        }
    }

    private void setUserIcon(){
        String fileName = String.format(res.getString(R.string.user_icon_path_format),info.getUserID());
        File filePath = getFileStreamPath(fileName);
        if ( filePath.exists() ){
            Drawable image = Drawable.createFromPath(filePath.toString());
            if ( image != null ){
                userIcon = image;
                imageViewUserIcon.setImageDrawable( userIcon);
            }
        }else{
            new BackGround(REQUEST_USER_ICON).execute();
        }
    }


    private void openSetting(){
        Intent intent = new Intent(MenuActivity.this, SettingActivity.class);
        intent.putExtra(InfoStore.INTENT_SETTING,(InfoStore)info);
        startActivity(intent);
        finish();
    }

    private void openRanking(){
        if ( !info.isLogin() ){
            Toast.makeText(MenuActivity.this,res.getString(R.string.no_login_warning),Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MenuActivity.this, RankingActivity.class);
        intent.putExtra(InfoStore.INTENT_RANKING,info);
        startActivity(intent);
    }

    private void openMyList(){
        if ( !info.isLogin() ){
            Toast.makeText(MenuActivity.this,res.getString(R.string.no_login_warning),Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MenuActivity.this, MyListActivity.class);
        intent.putExtra(InfoStore.INTENT_MYLIST,info);
        startActivity(intent);
    }

    private void openSearch(){
        Intent intent = new Intent(MenuActivity.this, SearchActivity.class);
        intent.putExtra(InfoStore.INTENT_SEARCH,info);
        startActivity(intent);
    }
}

package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo on 2016/12/10.
 *
 * this is an activity for setting
 *
 * references;
 * how to catch event when back button is tapped : http://android.keicode.com/basics/how-to-catch-back-button.php
 */

public class SettingActivity extends AppCompatActivity {

    private InfoStore info;
    private Resources res;

    private Button buttonEditAccount;
    private Button buttonChangeAccount;
    private Switch switchAuto;
    private Switch switchRecommend;
    private RadioGroup radioGroup;
    private LinearLayout youtubeSettingContainer;
    private View youtubeSetting;
    private Map<Integer,Integer>radioMap;

    private SharedPreferences data;

    private final int REQUEST_EDIT = 0;
    private final int REQUEST_CHANGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        res = getResources();
        data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);

        setTitle(res.getString(R.string.setting));

        receiveIntent(getIntent());

        buttonEditAccount = (Button)findViewById(R.id.buttonSettingEditAccount);
        buttonChangeAccount = (Button)findViewById(R.id.buttonSettingChangeAccount);
        switchAuto = (Switch)findViewById(R.id.switchSettingAutoLogin);
        switchRecommend = (Switch)findViewById(R.id.switchSettingRecommend);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroupSettingVideoChoice);
        youtubeSettingContainer = (LinearLayout) findViewById(R.id.linearLayoutSettingYoutubeContainer);
        //relations of ID and radio button
        radioMap = new HashMap<Integer, Integer>(){
            {
                put(R.id.radioSettingVideoOther,InfoStore.VIDEO_OTHER);
                put(R.id.radioSettingVideoSearch,InfoStore.VIDEO_SEARCH);
                put( R.id.radioSettingVideoNothing,InfoStore.VIDEO_NOTHING);
            }
        };

        buttonEditAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAccountList(REQUEST_EDIT);
            }
        });

        buttonChangeAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showAccountList(REQUEST_CHANGE);
            }
        });

        switchAuto.setChecked(info.isAutoLogin());
        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setAutoLogin(isChecked);
            }
        });

        switchRecommend.setChecked(info.isRecommend());
        switchRecommend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setRecommend(isChecked);
            }
        });

        ((RadioButton)findViewById(getRadioID(info.getVideoChoice()))).setChecked(true);
        if ( info.getVideoChoice() == InfoStore.VIDEO_SEARCH ){
            showYouTubeSettingView();
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int video = getVideoChoice(checkedId);
                info.setVideoChoice(video);
                if ( video == InfoStore.VIDEO_SEARCH ){
                    showYouTubeSettingView();
                }else{
                    if ( youtubeSetting != null ){
                        youtubeSettingContainer.removeView(youtubeSetting);
                        youtubeSetting = null;
                    }
                }
            }
        });

        //back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private int getRadioID(int video){
        for ( Integer key : radioMap.keySet()){
            if ( radioMap.get(key) == video ){
                return key;
            }
        }
        return 0;
    }

    private int getVideoChoice(int id){
        for ( Integer key : radioMap.keySet() ){
            if ( key == id ){
                return radioMap.get(key);
            }
        }
        return 0;
    }
    private void showYouTubeSettingView(){
        Context context = SettingActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.viewSettingYoutubeRoot);
        youtubeSetting = inflater.inflate(R.layout.view_setting_youtube, root, true);
        youtubeSettingContainer.addView(youtubeSetting);
        Switch switchYoutubeWatch = (Switch)youtubeSetting.findViewById(R.id.switchYoutubeSettingWatch);
        final Switch switchYoutubeComment = (Switch)youtubeSetting.findViewById(R.id.switchYoutubeSettingComment);
        switchYoutubeWatch.setChecked(info.isYouTubeWatch());
        switchYoutubeComment.setChecked(info.isYouTubeComment());
        switchYoutubeComment.setEnabled(info.isYouTubeWatch());
        switchYoutubeWatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setYouTubeWatch(isChecked);
                switchYoutubeComment.setEnabled(isChecked);
            }
        });
        switchYoutubeComment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setYouTubeComment(isChecked);
            }
        });
    }

    private void showAccountList(final int request){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        switch ( request ){
            case REQUEST_EDIT:
                builder.setTitle(res.getString(R.string.setting_edit_account));
                break;
            case REQUEST_CHANGE:
                builder.setTitle(res.getString(R.string.setting_change_account));
                break;
            default:
        }
        Context context = SettingActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogAccountListRoot);
        View  v = inflater.inflate(R.layout.dialog_setting_account, root, true);

        //set listView
        ListView listView = (ListView)v.findViewById(R.id.listViewAccount);
        List<AccountInfo> list = getAccountList();
        listView.setAdapter(new AccountListAdapter(SettingActivity.this, list, res.getString(R.string.login_status_in)));


        //show dialog
        builder.setView(v);
        builder.setMessage(res.getString(R.string.setting_list_mes));
        builder.setNegativeButton("Cancel", null);
        final Dialog dialog = builder.create();
        dialog.show();

        //ListView item selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get item
                AccountListAdapter adapter = (AccountListAdapter) parent.getAdapter();
                AccountInfo accountInfo = adapter.getItem(position);
                Toast.makeText(SettingActivity.this,accountInfo.getName(),Toast.LENGTH_SHORT).show();
                if ( dialog != null ){
                    dialog.dismiss();
                }
                switch ( request ){
                    case REQUEST_EDIT:
                        showEditAccount(accountInfo);
                        break;
                    case REQUEST_CHANGE:
                        if ( accountInfo.isValid() ) {
                            changeAccount(accountInfo);
                        }
                        break;
                    default:
                }
            }
        });
    }

    private void showEditAccount(final AccountInfo accountInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle(res.getString(R.string.setting_edit_account));
        Context context = SettingActivity.this;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup)findViewById(R.id.dialogAddRoot);
        View  v = inflater.inflate(R.layout.dialog_add_account, root, true);
        final EditText editMail = (EditText) v.findViewById(R.id.editTextMail);
        final EditText editPass = (EditText) v.findViewById(R.id.editTextPass);
        if ( accountInfo.isValid() ){
            editMail.setText(accountInfo.getMail());
        }
        builder.setView(v);
        builder.setMessage(res.getString(R.string.setting_list_mes));
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                info.saveMail(accountInfo.getIndex(),editMail.getText().toString());
                info.savePass(accountInfo.getIndex(),editPass.getText().toString());
                info.save();
            }
        });
        builder.create();
        builder.show();
    }

    private void changeAccount(AccountInfo accountInfo){
        info.setLoginTarget( accountInfo.getIndex());
        info.setCookieStore(null);
        info.save();
        Intent intent = new Intent(SettingActivity.this, MenuActivity.class);
        intent.putExtra(InfoStore.INTENT_MENU,info);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                info.save();
                Intent intent = new Intent(SettingActivity.this, MenuActivity.class);
                intent.putExtra(InfoStore.INTENT_MENU,info);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ( keyCode == KeyEvent.KEYCODE_BACK){
            //save change of setting
            info.save();
            Intent intent = new Intent(SettingActivity.this, MenuActivity.class);
            intent.putExtra(InfoStore.INTENT_MENU,info);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore)intent.getSerializableExtra(InfoStore.INTENT_SETTING);
            if ( info != null ){
                receive = true;
                this.info = info;
                this.info.initialize(data);
            }
        }
        if ( !receive ){
            Log.d("setting","failed to receive intent");
            finish();
        }
    }

    private List<AccountInfo> getAccountList(){
        List<AccountInfo> list = new ArrayList<AccountInfo>();
        for ( int i=0 ; i<info.getNum() ; i++){
            String name = res.getString(R.string.setting_list_name) + (i+1);
            String mail = info.getMail(i);
            if ( i == info.getLoginTarget() ){
                list.add(new AccountInfo(i,name,mail,true));
            }else{
                list.add(new AccountInfo(i,name,mail,false));
            }
        }
        return list;
    }
}

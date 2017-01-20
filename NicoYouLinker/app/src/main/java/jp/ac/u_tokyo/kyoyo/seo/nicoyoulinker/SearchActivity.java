package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seo on 2016/12/17.
 *
 * this activity extending CustomListActivity searches video and show
 *
 * reference;
 * create view : http://www.javadrive.jp/android/activity/index5.html
 * usage of Calendar.class : http://www.javaroad.jp/java_date2.htm
 * usage of DatePickerDialog : http://techbooster.jpn.org/andriod/application/8234/
 * usage of TimePickerDialog : http://techbooster.org/android/ui/11506/
 * Niconico search API guide : http://site.nicovideo.jp/search-api-docs/snapshot.html
 * usage of NumberFormat.class : http://java-reference.sakuraweb.com/java_number_format.html
 */

public class SearchActivity extends CustomListActivity {

    private List<VideoInfo> list;

    private ListView listViewSearch;
    private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        receiveIntent(getIntent());
        res = getResources();
        setTitle(res.getString(R.string.search_title));

        listViewSearch = (ListView)findViewById(R.id.listViewSearch);
        buttonSearch = (Button)findViewById(R.id.buttonSearch);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchSetting();
            }
        });

        showSearchSetting();
    }

    protected void receiveIntent(Intent intent){
        boolean receive = false;
        if ( intent != null ){
            InfoStore info = (InfoStore)intent.getSerializableExtra(InfoStore.INTENT_SEARCH);
            if ( info != null ){
                receive = true;
                this.info = info;
            }
        }
        if ( !receive ){
            Log.d("search","failed to receive intent");
            finish();
        }
    }

    //manage many views using map
    //following constants are keys of the map
    private Context context = SearchActivity.this;
    private final Map<String,View> viewMap = new HashMap<String,View>();
    private final String EDIT_KEY_DIRECT = "editKeyDirect";
    private final String KEY_ROOT = "keyRoot";
    private final String KEY_DIRECT_ROOT = "keyDirectRoot";
    private final String KEY_CELL_ROOT = "keyCellRoot";
    private final String ADD_KEY = "addKey";
    private final String DELETE_KEY = "deleteKey";
    private final String KEY_CONTAINER = "keyContainer";
    private final String FILTER_CONTAINER = "filterContainer";
    private final String FILTER_ROOT = "filterRoot";
    private final String FILTER_ID_FROM = "filterIdFrom";
    private final String FILTER_ID_TO = "filterIdTo";
    private final String FILTER_TAGS = "filterTags";
    private final String FILTER_GENRE = "filterGenre";
    private final String FILTER_VIEW_FROM = "filterViewFrom";
    private final String FILTER_VIEW_TO = "filterViewTo";
    private final String FILTER_MYLIST_FROM = "filterMyListFrom";
    private final String FILTER_MYLIST_TO = "filterMyLisTo";
    private final String FILTER_COMMENT_FROM = "filterCommentFrom";
    private final String FILTER_COMMENT_TO = "filterCommentTo";
    private final String FILTER_LENGTH_FROM = "filterLengthFrom";
    private final String FILTER_LENGTH_TO = "filterLengthTo";
    private final List<View> cellList = new ArrayList<View>();
    private final List<Spinner> spinnerList = new ArrayList<Spinner>();
    private final List<EditText> editTextList = new ArrayList<EditText>();
    private final Map<String,String> dateMap = new HashMap<String,String>();
    private final String FILTER_DATE_FROM = "filterDateFrom";
    private final String FILTER_DATE_TO = "filterDateTo";

    private void showSearchSetting(){
        //initialize
        cellList.clear();
        spinnerList.clear();
        editTextList.clear();
        viewMap.clear();
        dateMap.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle(res.getString(R.string.search_prompt));

        ViewGroup root = (ViewGroup)findViewById(R.id.dialogSearchRoot);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_search, root, true);
        viewMap.put(KEY_CONTAINER, v.findViewById(R.id.viewContainerSearchKey));
        viewMap.put(FILTER_CONTAINER, v.findViewById(R.id.viewContainerSearchFilter));
        final CheckBox checkTags = (CheckBox)v.findViewById(R.id.checkBoxSearchTags);
        final Switch switchKeyDirect = (Switch)v.findViewById(R.id.switchSearchDirect);
        final Spinner spinnerSortField = (Spinner)v.findViewById(R.id.spinnerSearchSort);
        final ToggleButton toggleSortDirection = (ToggleButton)v.findViewById(R.id.toggleSearchSort);
        final EditText editTextLimit = (EditText)v.findViewById(R.id.editTextSearchLimit);
        final Switch switchFilter = (Switch)v.findViewById(R.id.switchSearchFilter);

        //at first
        setSearchKey(true);

        //set query directly?
        switchKeyDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setSearchDirectKey();
                } else {
                    setSearchKey(false);
                }
            }
        });

        //set filter?
        switchFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( isChecked ){
                    setFilter();
                }else{
                    closeFilter();
                }
            }
        });

        //build dialog
        final String[] keyOperator= new String[]{" "," OR "," -"};
        builder.setView(v);
        builder.setPositiveButton(res.getString(R.string.search_go), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get search setting and check whether it is valid or not
                //key words
                boolean valid = false;
                String keyWord = "";
                if ( switchKeyDirect.isChecked() ){
                    keyWord = ((EditText)viewMap.get(EDIT_KEY_DIRECT)).getText().toString();
                    if ( !keyWord.isEmpty() ){
                        valid = true;
                    }
                }else{
                    StringBuilder builder = new StringBuilder();
                    String input = editTextList.get(0).getText().toString();
                    if ( !input.isEmpty() ){
                        builder.append(input);
                        valid = true;
                    }
                    for ( int i=1 ; i<editTextList.size() ; i++){
                        String operator = keyOperator[spinnerList.get(i).getSelectedItemPosition()];
                        String key = editTextList.get(i).getText().toString();
                        if ( !key.isEmpty() ){
                            if ( valid ){
                                builder.append(operator);
                            }
                            valid = true;
                            if ( key.indexOf("\\s") < 0){
                                builder.append(key);
                            }else{
                                builder.append("\"");
                                builder.append(key);
                                builder.append("\"");
                            }
                        }
                    }
                    keyWord = builder.toString();
                }
                if ( ! valid ){
                    showToast(res.getString(R.string.search_key_warning));
                    return;
                }
                //is tags search?
                boolean tags = checkTags.isChecked();
                //sort param
                int index = spinnerSortField.getSelectedItemPosition();
                String sortParam = res.getStringArray(R.array.search_sort_fields)[index];
                if ( toggleSortDirection.isChecked() ){
                    sortParam = "+"+sortParam;
                }else{
                    sortParam = "-"+sortParam;
                }
                //limit number
                int limit = parseNaturalNumber(editTextLimit.getText().toString());
                if ( limit < 10 || limit > 100 ){
                    showToast(res.getString(R.string.search_limit_warning));
                    return;
                }
                //filter
                String filterParam = "";
                if ( switchFilter.isChecked() ){
                    StringBuilder builder = new StringBuilder();
                    String from,to;
                    //id
                    from = ((EditText)viewMap.get(FILTER_ID_FROM)).getText().toString();
                    to = ((EditText)viewMap.get(FILTER_ID_TO)).getText().toString();
                    setInterval(from,to,builder,"&filters[contentId][gte]=sm","&filters[contentId][lte]=sm");
                    //tags
                    from = ((EditText)viewMap.get(FILTER_TAGS)).getText().toString();
                    if ( !from.isEmpty() ){
                        builder.append("&filters[tags][0]=");
                        builder.append(from);
                    }
                    //genre
                    int spinnerIndex = ((Spinner)viewMap.get(FILTER_GENRE)).getSelectedItemPosition();
                    if ( spinnerIndex > 0 ){
                        builder.append("&filters[categoryTags][0]=");
                        builder.append(res.getStringArray(R.array.spinner_array_genre)[spinnerIndex]);
                    }
                    //view counter
                    from = ((EditText)viewMap.get(FILTER_VIEW_FROM)).getText().toString();
                    to = ((EditText)viewMap.get(FILTER_VIEW_TO)).getText().toString();
                    setInterval(from,to,builder,"&filters[viewCounter][gte]=","&filters[viewCounter][lte]=");
                    //myList counter
                    from = ((EditText)viewMap.get(FILTER_MYLIST_FROM)).getText().toString();
                    to = ((EditText)viewMap.get(FILTER_MYLIST_TO)).getText().toString();
                    setInterval(from,to,builder,"&filters[mylistCounter][gte]=","&filters[mylistCounter][lte]=");
                    //comment counter
                    from = ((EditText)viewMap.get(FILTER_COMMENT_FROM)).getText().toString();
                    to = ((EditText)viewMap.get(FILTER_COMMENT_TO)).getText().toString();
                    setInterval(from,to,builder,"&filters[commentCounter][gte]=","&filters[commentCounter][lte]=");
                    //date
                    from = dateMap.get(FILTER_DATE_FROM);
                    to = dateMap.get(FILTER_DATE_TO);
                    if ( from != null ){
                        builder.append("&filters[startTime][gte]=");
                        builder.append(from);
                    }
                    if ( to != null ){
                        builder.append("&filters[startTime][lte]=");
                        builder.append(to);
                    }
                    //length
                    from = ((EditText)viewMap.get(FILTER_LENGTH_FROM)).getText().toString();
                    to = ((EditText)viewMap.get(FILTER_LENGTH_TO)).getText().toString();
                    setInterval(from,to,builder,"&filters[lengthSeconds][gte]=","&filters[lengthSeconds][lte]=");
                    filterParam = builder.toString();
                }
                new BackGround(keyWord,tags,sortParam,limit,filterParam).execute();
            }
        });
        builder.create();
        builder.show();
    }

    private void setInterval(String min, String max, StringBuilder builder, String minParam, String maxParam){
        int s = parseNaturalNumber(min);
        int e = parseNaturalNumber(max);
        if ( 0 < s ){
            builder.append(minParam);
            builder.append(min);
        }
        if ( 0 < e && s <= e ){
            builder.append(maxParam);
            builder.append(max);
        }
    }

    private int parseNaturalNumber(String target){
        try{
            int n = Integer.parseInt(target);
            if ( n > 0){
                return n;
            }
            return 0;
        }catch (NumberFormatException e){
            return 0;
        }
    }

    private void setSearchKey(boolean initial){
        //delete
        if ( !initial ){
            ((LinearLayout)viewMap.get(KEY_CONTAINER)).removeView(viewMap.get(KEY_DIRECT_ROOT));
            cellList.clear();
            spinnerList.clear();
            editTextList.clear();
        }
        //inflate
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup viewSearchKeyRoot = (ViewGroup) findViewById(R.id.viewSearchKeyRoot);
        View viewSearchKey = inflater.inflate(R.layout.view_key, viewSearchKeyRoot, true);
        ((LinearLayout)viewMap.get(KEY_CONTAINER)).addView(viewSearchKey);
        View addKey = viewSearchKey.findViewById(R.id.buttonSearchKeyAdd);
        View deleteKey = viewSearchKey.findViewById(R.id.buttonSearchKeyRemove);
        final View keyCellRoot = viewSearchKey.findViewById(R.id.searchKeyRoot);
        //add or delete keyWord editText if requested
        addKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup root = (ViewGroup)findViewById(R.id.cellSearchKeyRoot);
                View cell = inflater.inflate(R.layout.cell_search_key,root,true);
                ((LinearLayout)keyCellRoot).addView(cell);
                cellList.add(cell);
                spinnerList.add((Spinner)cell.findViewById(R.id.spinnerSearchKeyOperator));
                editTextList.add((EditText)cell.findViewById(R.id.editTextSearchKey));
            }
        });
        deleteKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( cellList.size() > 1 ){
                    int lastIndex = cellList.size()-1;
                    ((LinearLayout)keyCellRoot).removeView(cellList.get(lastIndex));
                    cellList.remove(lastIndex);
                    spinnerList.remove(lastIndex);
                    editTextList.remove(lastIndex);
                }
            }
        });
        cellList.add(null);
        spinnerList.add(null);
        editTextList.add((EditText) viewSearchKey.findViewById(R.id.editTextSearchKey));
        viewMap.put(KEY_ROOT, viewSearchKey);
        viewMap.put(KEY_CELL_ROOT, keyCellRoot);
        viewMap.put(ADD_KEY, addKey);
        viewMap.put(DELETE_KEY, deleteKey);
    }

    private void setSearchDirectKey(){
        //delete
        ((LinearLayout)viewMap.get(KEY_CONTAINER)).removeView(viewMap.get(KEY_ROOT));
        //inflate
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup) findViewById(R.id.viewSearchKeyDirectRoot);
        View v = inflater.inflate(R.layout.view_key_direct, root, true);
        ((LinearLayout)viewMap.get(KEY_CONTAINER)).addView(v);
        viewMap.put(KEY_DIRECT_ROOT, v);
        viewMap.put(EDIT_KEY_DIRECT, v.findViewById(R.id.editTextSearchKeyDirect));
    }

    private void setFilter(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = (ViewGroup) findViewById(R.id.viewSearchFilterRoot);
        View view = inflater.inflate(R.layout.view_filter, root, true);
        ((LinearLayout)viewMap.get(FILTER_CONTAINER)).addView(view);
        viewMap.put(FILTER_ROOT,view);
        //set DateDialog and TimeDialog
        View dateFrom = view.findViewById(R.id.buttonSearchFilterDateFrom);
        View dateTo = view.findViewById(R.id.buttonSearchFilterDateTo);
        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateTime(FILTER_DATE_TO);
            }
        });
        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateTime(FILTER_DATE_FROM);
            }
        });
        //register view to map
        viewMap.put(FILTER_DATE_TO, dateTo);
        viewMap.put(FILTER_DATE_FROM, dateFrom);
        viewMap.put(FILTER_ID_FROM, view.findViewById(R.id.editTextSearchFilterIdFrom));
        viewMap.put(FILTER_ID_TO, view.findViewById(R.id.editTextSearchFilterIdTo));
        viewMap.put(FILTER_TAGS, view.findViewById(R.id.editTextSearchFilterTags));
        viewMap.put(FILTER_GENRE, view.findViewById(R.id.spinnerSearchFilterGenre));
        viewMap.put(FILTER_VIEW_FROM, view.findViewById(R.id.editTextSearchFilterViewFrom));
        viewMap.put(FILTER_VIEW_TO, view.findViewById(R.id.editTextSearchFilterViewTo));
        viewMap.put(FILTER_MYLIST_FROM, view.findViewById(R.id.editTextSearchFilterMyListFrom));
        viewMap.put(FILTER_MYLIST_TO, view.findViewById(R.id.editTextSearchFilterMyListTo));
        viewMap.put(FILTER_COMMENT_FROM, view.findViewById(R.id.editTextSearchFilterCommentFrom));
        viewMap.put(FILTER_COMMENT_TO, view.findViewById(R.id.editTextSearchFilterCommentTo));
        viewMap.put(FILTER_LENGTH_FROM, view.findViewById(R.id.editTextSearchFilterLengthFrom));
        viewMap.put(FILTER_LENGTH_TO, view.findViewById(R.id.editTextSearchFilterLengthTo));
    }

    private void getDateTime(final String mapKey){
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(SearchActivity.this, android.R.style.Theme_Material_Light_Dialog,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        new TimePickerDialog(SearchActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        String date = getDateString(year,month,dayOfMonth,hourOfDay,minute,true);
                                        showToast(date);
                                        dateMap.put(mapKey,date);
                                        ((Button)viewMap.get(mapKey)).setText(getDateString(year,month,dayOfMonth,hourOfDay,minute,false));
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
                    }
                },
                calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.show();
    }

    private String getDateString(int year, int month, int dayOfMonth, int hourOfDay, int minute, boolean iso){
        if ( iso ){
            //caution : URL encoder of apache.HttpGet fails to convert "+", so have to be replaced with "%2b" beforehand
            return String.format("%04d-%02d-%02dT%02d:%02d:00%%2b09:00",year,month,dayOfMonth,hourOfDay,minute);
        }else{
            return String.format("%04d/%02d/%02d\n%02d:%02d",year,month,dayOfMonth,hourOfDay,minute);
        }
    }

    private void closeFilter(){
        ((LinearLayout)viewMap.get(FILTER_CONTAINER)).removeView(viewMap.get(FILTER_ROOT));
    }

    private void showToast(String mes){
        Toast.makeText(SearchActivity.this, mes, Toast.LENGTH_SHORT).show();
    }

    private class BackGround extends AsyncTask<String, Void, String> {


        /**
         * this class searches videos
         *
         * Caution;
         * using search API does not need login session
         * search result returns in Json format
         *
         * References;
         * Niconico search API guide : http://site.nicovideo.jp/search-api-docs/snapshot.html
         * usage of NumberFormat.class : http://java-reference.sakuraweb.com/java_number_format.html
         */

        private ProgressDialog progress = null;
        private boolean success = false;
        private String keyWord;
        private boolean tagsSearch;
        private String sortParam;
        private int limit;
        private String filterParam;

        public BackGround(String key, boolean tags, String sortParam, int limit, String filterParam){
            this.keyWord = key;
            this.tagsSearch = tags;
            this.sortParam = sortParam;
            this.limit = limit;
            this.filterParam = filterParam;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(SearchActivity.this);
            progress.setMessage(res.getString(R.string.search_progress));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String ... params){
            String url = res.getString(R.string.search_url);
            String appName = res.getString(R.string.app_name);
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            builder.append(keyWord);
            if ( tagsSearch ){
                builder.append("&targets=tagsExact");
            }else{
                builder.append("&targets=title,description,tags");
            }
            builder.append("&fields=contentId,title,description,tags,viewCounter,mylistCounter,commentCounter,startTime,lengthSeconds");
            builder.append(filterParam);
            builder.append("&_sort=");
            builder.append(sortParam);
            builder.append("&_limit=");
            builder.append(limit);
            builder.append("&_context=");
            builder.append(appName);
            String path = builder.toString();
            String res = new HttpResponseGetter().getResponse(path);
            if ( res == null){
                return null;
            }
            list = SearchVideoInfo.parse(res);
            if ( list != null ){
                success = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (progress != null) {
                progress.cancel();
                progress = null;
            }
            if ( success ){
                listViewSearch.setAdapter(new RankingListAdapter(SearchActivity.this, list, res));
                listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RankingListAdapter adapter = (RankingListAdapter)parent.getAdapter();
                        VideoInfo info = adapter.getItem(position);
                        onListItemClicked(info);
                    }
                });
            }else{
                Toast.makeText(SearchActivity.this, res.getString(R.string.search_fail),Toast.LENGTH_SHORT).show();
            }
        }
    }

}

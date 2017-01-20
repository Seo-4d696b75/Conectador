package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Seo on 2016/12/11.
 * this adapter class adapts AccountInfo.class for ListView
 * and should be passed in ListView.setAdapter()
 *
 * note;
 * this class is based on the lecture material: MessageAdapter.java in UTdroid_ChatApp-master
 */

public class AccountListAdapter extends ArrayAdapter<AccountInfo> {

    private LayoutInflater inflater;
    private String selectedMes;

    public AccountListAdapter(Context context, List<AccountInfo> list, String selected) {
        super(context, 0, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.selectedMes = selected;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.cell_account, null);
        }
        final AccountInfo item = this.getItem(position);
        if (item != null) {
            TextView name = (TextView)view.findViewById(R.id.textViewCellName);
            TextView mail = (TextView)view.findViewById(R.id.textViewCellMail);
            TextView selected = (TextView)view.findViewById(R.id.textViewCellSelected);
            name.setText(item.getName());
            mail.setText(item.getMail());
            if ( item.isSelected() ){
                selected.setText(selectedMes);
            }else{
                selected.setText("");
            }
        }

        return view;
    }
}

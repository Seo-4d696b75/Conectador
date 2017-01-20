package jp.ac.u_tokyo.kyoyo.seo.nicoyoulinker;

/**
 * Created by Seo on 2016/12/11.
 * when information of accounts is shown in listView,
 * this object keeps each field of the account.
 */

public class AccountInfo {

    private int index;
    private String name;
    private String mail;
    private boolean valid;
    private boolean selected;

    //initialize all the fields in the constructor
    public AccountInfo (int index, String name, String mail, boolean selected){
        this.index = index;
        this.name = name;
        if ( mail == null || mail.isEmpty() ){
            //check whether or not mail address is valid
            mail = "登録なし";
            valid = false;
        }else{
            valid = true;
        }
        this.mail = mail;
        this.selected = selected;
    }

    //getter
    public boolean isValid(){
        //whether mail is valid?
        return valid;
    }
    public boolean isSelected(){
        //whether this account is focused now?
        return selected;
    }
    public int getIndex(){
        return index;
    }
    public String getName(){
        return name;
    }
    public String getMail(){
        return mail;
    }

}

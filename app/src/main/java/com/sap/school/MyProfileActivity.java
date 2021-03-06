package com.sap.school;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.prashantsolanki.secureprefmanager.SecurePrefManager;
import com.sap.handler.IWSCallHandler;
import com.sap.handler.ResponseStatus;
import com.sap.handler.ServerComHandler;
import com.sap.school.PojoClass.ProfilePojoClass;
import com.sap.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyProfileActivity extends BaseActivity implements View.OnClickListener{
    @BindView(R.id.fullName) TextView fullName;
    @BindView(R.id.userType) TextView userType;
    @BindView(R.id.dobText) TextView dobText;
    @BindView(R.id.classDetails) TextView classDetails;
    @BindView(R.id.genderDetails) TextView genderDetails;
    @BindView(R.id.languageDetails) TextView languageDetails;
    @BindView(R.id.religionDetails) TextView religionDetails;
    @BindView(R.id.guardianName) TextView guardianName;
    @BindView(R.id.guardianAddress) TextView guardianAddress;
    @BindView(R.id.llguardnname) LinearLayout llGuardnName;
    @BindView(R.id.llguardnaddr) LinearLayout llGuardnAddrs;
    @BindView(R.id.rlLine6) RelativeLayout rlLine6;
    @BindView(R.id.rlLine7) RelativeLayout rlLine7;
    RelativeLayout backButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        ButterKnife.bind(this);
        initView();
        setListner();
        getProfileData();
    }


    private void initView() {
        backButton=(RelativeLayout)findViewById(R.id.backButton);
        fullName.setText("");
        dobText.setText("");
        genderDetails.setText("");
        String userTypeString = SecurePrefManager.with(this)
                .get("designation")
                .defaultValue("unknown")
                .go();
        if (StringUtils.isEmpty(userTypeString)) {
            userType.setText("");
        }
        else {
            userType.setText(userTypeString);
        }
        classDetails.setText("");
        languageDetails.setText("");
        religionDetails.setText("");
        guardianName.setText("");
        guardianAddress.setText("");

        if(userTypeString.equals("Teacher")){
            llGuardnName.setVisibility(View.GONE);
            llGuardnAddrs.setVisibility(View.GONE);
            rlLine6.setVisibility(View.GONE);
            rlLine7.setVisibility(View.GONE);
        }

    }
    private void getProfileData(){
        {
            showProgressUI(AppConstants.Loading);
            String wsLink = AppConstants.BaseURL+"Profile";
            //web method call
            JSONObject loginJson = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            String roll_id;
            roll_id = SecurePrefManager.with(this)
                    .get("roll_id")
                    .defaultValue("unknown")
                    .go(); // getting String

            String user_id = SecurePrefManager.with(this)
                    .get("user_id")
                    .defaultValue("unknown")
                    .go();;
            try {
                loginJson.put("role_id",roll_id );
                loginJson.put("user_id", user_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(loginJson);
            JSONObject ws_dataObj = new JSONObject();
            try {
                ws_dataObj.put("WS_DATA", jsonArray);
                ws_dataObj.put("WS_CODE", "110");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String json = ws_dataObj.toString();
            ServerComHandler.getInstance().wsCallJsonBy(wsLink,json, new IWSCallHandler() {
                @Override
                public void responseStatus(final int status, final Object data) {
                    if (status == 200) {
                        // List<NotificationItem> arrTmp = new ArrayList<>();
                        try{
                            ResponseStatus responseStatus = new ResponseStatus((String)data);
                            if (responseStatus.isSuccess()) {
                                dismissProgressUI();
                                JSONArray jsonArray = responseStatus.jsonObject.getJSONArray("result");
                                 JSONObject jsonObjItm = jsonArray.getJSONObject(0);
                                Gson gson = new Gson();
                                String userJson = gson.toJson(jsonObjItm);
                                final ProfilePojoClass profileObject = new ProfilePojoClass();
                                profileObject.setName(responseStatus.parseName());
                                profileObject.setDob(responseStatus.parseDOB());
                                profileObject.setGender(responseStatus.parseGender());

                                MyProfileActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressUI();
                                        updateUI(profileObject);
                                    }
                                });
                            }
                            else {
                                ToastUtils.showShort(responseStatus.response_message);
                                dismissProgressUI();
                            }
                            MyProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressUI();
                                }
                            });
                        }catch (Exception ex){ex.printStackTrace();}


                    }else{
                        dismissProgressUI();
                    }
                }
            });

        }
    }

    private void updateUI(ProfilePojoClass profileData) {
        fullName.setText(profileData.getName());
        dobText.setText(profileData.getDob());
        if(profileData.getGender().equals("M")){
            genderDetails.setText("Male");
        }

    }

    private void setListner() {
        backButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.backButton:
                finish();
                break;
        }
    }
}

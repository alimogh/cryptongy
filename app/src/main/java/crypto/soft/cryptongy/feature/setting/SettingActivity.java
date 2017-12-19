package crypto.soft.cryptongy.feature.setting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import crypto.soft.cryptongy.R;

public class SettingActivity extends MvpActivity<SettingView, SettingPresenter> implements SettingView {
    private ToggleButton tgbSound, tgbVibration, tgbAutomaticSync;
    private Notification notification;
    private LinearLayout lnlInterval;
    private EditText edtInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initToolbar();
        findViews();
        setListner();
        intervalTextwatcher();
        presenter.getNotification();
    }

    @NonNull
    @Override
    public SettingPresenter createPresenter() {
        return new SettingPresenter(this);
    }

    @Override
    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView txtTitle = toolbar.findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.settings);
    }

    @Override
    public void findViews() {
        tgbSound = findViewById(R.id.tgbSound);
        tgbVibration = findViewById(R.id.tgbVibration);
        tgbAutomaticSync = findViewById(R.id.tgbAutomaticSync);
        lnlInterval = findViewById(R.id.lnlInterval);
        edtInterval = findViewById(R.id.edtInterval);
    }

    @Override
    public void setListner() {
        tgbSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notification.setSound(b);
                presenter.updateNotification(notification);
            }
        });

        tgbVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notification.setVibrate(b);
                presenter.updateNotification(notification);
            }
        });

        tgbAutomaticSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    lnlInterval.setVisibility(View.VISIBLE);
                else
                    lnlInterval.setVisibility(View.GONE);
                notification.setAutomSync(b);
                presenter.updateNotification(notification);
            }
        });
    }

    @Override
    public void setNotification(Notification notification) {
        this.notification = notification;
        tgbSound.setChecked(notification.isSound());
        tgbVibration.setChecked(notification.isVibrate());
        tgbAutomaticSync.setChecked(notification.isAutomSync());
        if (notification.isAutomSync())
            lnlInterval.setVisibility(View.VISIBLE);
        else
            lnlInterval.setVisibility(View.GONE);
        edtInterval.setText(String.valueOf(notification.getSyncInterval()));
    }

    @Override
    public void intervalTextwatcher() {
        edtInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (!TextUtils.isEmpty(s)) {
                    int interval=Integer.parseInt(s);
                    if (interval>0) {
                        notification.setSyncInterval(Integer.parseInt(s));
                        presenter.updateNotification(notification);
                    }else
                        edtInterval.setError("Should be greater than 0");
                }else
                    edtInterval.setError("Cannot be empty");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

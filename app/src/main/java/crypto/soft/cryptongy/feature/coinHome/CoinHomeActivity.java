package crypto.soft.cryptongy.feature.coinHome;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.MvpActivity;

import crypto.soft.cryptongy.R;
import crypto.soft.cryptongy.feature.coin.CoinFragment;
import crypto.soft.cryptongy.feature.order.OrderReceiver;
import crypto.soft.cryptongy.feature.shared.adapter.MainPagerAdaptor;
import crypto.soft.cryptongy.utils.GlobalUtil;
import crypto.soft.cryptongy.utils.HideKeyboard;
import crypto.soft.cryptongy.utils.ProgressDialogFactory;

import static crypto.soft.cryptongy.feature.home.HomeFragment.EXCHANGE_VALUE;

public class CoinHomeActivity extends MvpActivity<CoinHomeView, CoinHomePresenter> implements CoinHomeView {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    CoinFragment coinFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_home);
        new HideKeyboard(this).setupUI(findViewById(android.R.id.content));
        initToolbar();
        findViews();
        initTab();
        setTitle();

    }

    @NonNull
    @Override
    public CoinHomePresenter createPresenter() {
        return new CoinHomePresenter();
    }

    @Override
    public void setTitle() {
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.app_name);
    }

    @Override
    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void findViews() {
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
    }

    @Override
    public void initTab() {
        MainPagerAdaptor adapter = new MainPagerAdaptor(getSupportFragmentManager());
         coinFragment = new CoinFragment();
        Bundle bundle = new Bundle();
        String coinName = getIntent().getStringExtra("COIN_NAME");
        bundle.putString("COIN_NAME", coinName);
        bundle.putString(EXCHANGE_VALUE,getIntent().getStringExtra(EXCHANGE_VALUE));
        coinFragment.setArguments(bundle);
        adapter.addFragment(coinFragment, "Coin");
//        adapter.addFragment(new AlertFragment(), "Alert");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        ProgressDialogFactory.dismiss();
        GlobalUtil.stopAlarm(OrderReceiver.class, this);
//        Log.d("Cryptongy", "alarm stoped");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        coinFragment.getPresenter().closeWebSocket();
    }
}
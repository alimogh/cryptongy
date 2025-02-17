package crypto.soft.cryptongy.feature.home;

import android.content.Context;
import android.util.Log;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import crypto.soft.cryptongy.R;
import crypto.soft.cryptongy.feature.setting.Notification;
import crypto.soft.cryptongy.feature.shared.json.market.MarketSummaries;
import crypto.soft.cryptongy.feature.shared.json.market.Result;
import crypto.soft.cryptongy.feature.shared.listner.OnMultiFinishListner;
import crypto.soft.cryptongy.utils.CoinApplication;
import crypto.soft.cryptongy.utils.GlobalConstant;

/**
 * Created by tseringwongelgurung on 11/27/17.
 */

public class HomePresenter extends MvpBasePresenter<HomeView> implements OnMultiFinishListner<List<Result>, MarketSummaries, String> {
    public String TAG =getClass().getSimpleName();
    private static Timer timer;
    private HomeInteractor homeInteractor;
    private Context context;
    private List<Result> prevResults = new ArrayList<>();
    private List<Result> binancePrevResults = new ArrayList<>();
    private boolean isStarted = false;
    private String exchangeValue;


    public HomePresenter(Context context) {
        this.homeInteractor = new HomeInteractor();
        this.context = context;
        isStarted = false;
    }

    public void loadSummaries() {
        if (getView() != null)
            getView().showProgressBar();

        exchangeValue = GlobalConstant.Exchanges.BITTREX;
        homeInteractor.loadSummary(context, this);
    }

    public void loadBinanceSummaries() {
        if (getView() != null)
            getView().showProgressBar();

        exchangeValue = GlobalConstant.Exchanges.BINANCE;
        homeInteractor.loadBinanceSummaryAPI(context, this);
    }

    @Override
    public void onComplete(List<Result> results, MarketSummaries marketSummaries, String exchange) {
        if (getView() != null) {
            results = setDrawable(results);
            prevResults = results;
            getView().setAdapter(results);
            getView().onSummaryDataLoad(marketSummaries, exchange);
            isStarted = true;
//            startTimer();
        }
    }

    @Override
    public void onFail(String error) {
        if (getView() != null) {
            getView().onSummaryLoadFailed();
            getView().hideProgressBar();
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            closeWebSocket();
            prevResults = null;
        }
    }

    public void startTimer( String exchangeValue) {
        this.exchangeValue = exchangeValue;
        if (timer != null) {
            timer.cancel();
            closeWebSocket();
            prevResults = null;
        }
        Notification notification = ((CoinApplication) context.getApplicationContext()).getNotification();
        if (notification.isAutomSync()) {
            Log.d(TAG, "startTimer: synValue "+notification.getSyncInterval());
            int timerInterval = notification.getSyncInterval() * 1000;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TickerTimer(), timerInterval,
                    timerInterval);
        }

    }

    private List<Result> setDrawable(List<Result> list) {
        for (int i = 0; i < list.size(); i++) {
            Result result = list.get(i);
            if (prevResults == null)
                result.setDrawable(R.drawable.seek_progress);
            else if (i < prevResults.size() && result != null) {
                Log.d(TAG, "setDrawable: previous result: "+prevResults.get(i).getLast().doubleValue() +" result last value : "+result.getLast().doubleValue() );
                if (result.getLast().doubleValue() < prevResults.get(i).getLast().doubleValue())
                    result.setDrawable(R.drawable.seek_progress_red);
                else if (result.getLast().doubleValue() > prevResults.get(i).getLast().doubleValue())
                    result.setDrawable(R.drawable.seek_progress_green);
                else if (result.getLast().doubleValue() == prevResults.get(i).getLast().doubleValue())
                    result.setDrawable(R.drawable.seek_progress);
            }
        }
        return list;
    }

    class TickerTimer extends TimerTask {
        boolean isComplete = true;
        @Override
        public void run() {

            Notification notification = ((CoinApplication) context.getApplicationContext()).getNotification();
            if (notification.isAutomSync() && isComplete) {
                isComplete = false;
                if (exchangeValue.equalsIgnoreCase(GlobalConstant.Exchanges.BITTREX)) {
                    homeInteractor.loadSummary(context, new OnMultiFinishListner<List<Result>, MarketSummaries, String>() {
                        @Override
                        public void onComplete(List<Result> results, MarketSummaries summaries, String exchange) {
                            if (getView() != null) {
                                results = setDrawable(results);
                                getView().setAdapter(results);
                                getView().onSummaryDataLoad(summaries, exchange);
                                prevResults = results;
                                isComplete = true;
                            }
                        }

                        @Override
                        public void onFail(String error) {

                        }
                    });
                }
                if (exchangeValue.equalsIgnoreCase(GlobalConstant.Exchanges.BINANCE)) {
                    homeInteractor.loadBinanceSummary(context, new OnMultiFinishListner<List<Result>, MarketSummaries, String >() {
                        @Override
                        public void onComplete(List<Result> results, MarketSummaries summaries, String exchange) {
                            if (getView() != null) {
                                results = setDrawable(results);
                                getView().setAdapter(results);
                                getView().onSummaryDataLoad(summaries, exchange);
                                prevResults = results;
                                isComplete = true;
                            }
                        }

                        @Override
                        public void onFail(String error) {

                        }
                    });
                }
            }

        }
    }


    public void closeWebSocket() {
        homeInteractor.closeWebSocket();
    }
}
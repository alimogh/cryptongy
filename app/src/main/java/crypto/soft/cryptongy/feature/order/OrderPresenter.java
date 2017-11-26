package crypto.soft.cryptongy.feature.order;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import crypto.soft.cryptongy.R;

import crypto.soft.cryptongy.feature.account.AccountFragment;
import crypto.soft.cryptongy.feature.account.CustomDialog;
import crypto.soft.cryptongy.feature.setting.SettingActivity;
import crypto.soft.cryptongy.feature.shared.json.action.Cancel;
import crypto.soft.cryptongy.feature.shared.json.openorder.OpenOrder;
import crypto.soft.cryptongy.feature.shared.json.orderhistory.OrderHistory;
import crypto.soft.cryptongy.feature.shared.json.orderhistory.Result;
import crypto.soft.cryptongy.feature.shared.listner.OnFinishListner;
import crypto.soft.cryptongy.feature.shared.module.Account;
import crypto.soft.cryptongy.utils.CoinApplication;
import crypto.soft.cryptongy.utils.GlobalUtil;

/**
 * Created by tseringwongelgurung on 11/23/17.
 */

public class OrderPresenter<T extends MvpView> extends MvpBasePresenter<T> {
    protected Context context;
    protected OrderInteractor interactor;
    protected boolean openOrder = false, orderHistory = false;

    public OrderPresenter(Context context) {
        this.context = context;
        interactor = new OrderInteractor();
    }

    public void onOptionItemClicked(int id) {
        switch (id) {
            case R.id.ic_setting:
                context.startActivity(new Intent(context, SettingActivity.class));
                break;
        }
    }

    public void onClicked(int id) {
        switch (id) {
            case R.id.imgSync:
                getData();
                break;
            case R.id.imgAccSetting:
                GlobalUtil.addFragment(context, new AccountFragment(), R.id.container, true);
                break;
        }
    }

    public void getData() {
        interactor.getAccount(new OnFinishListner<List<Account>>() {

            @Override
            public void onComplete(List<Account> result) {
                if (getView() != null) {
                    getV().showLoading(context.getString(R.string.fetch_msg));
                    setAccounts(result);
                }
                openOrder = false;
                orderHistory = false;
                getOpenOrders(false);
                getOrderHistory();
            }

            @Override
            public void onFail(String error) {
                CustomDialog.showMessagePop(context, error, null);
                if (getView() != null)
                    getV().showEmptyView();
            }
        });
    }

    public void setAccounts(List<Account> list) {
        CoinApplication app = (CoinApplication) context.getApplicationContext();
        boolean isRead = false, isTrade = false, isWithdraw = false;
        for (Account account : list) {
            if (account.getLabel().equals("Read")) {
                app.setReadAccount(account);
                isRead = true;
            } else if (account.getLabel().equals("Trade")) {
                app.setTradeAccount(account);
                isTrade = true;
            } else if (account.getLabel().equals("Withdraw")) {
                app.setWithdrawAccount(account);
                isWithdraw = true;
            }
        }
        if (getView() != null)
            if (isRead)
                getV().setLevel("Read");
            else if (isTrade)
                getV().setLevel("Trade");
            else if (isWithdraw)
                getV().setLevel("Withdraw");

    }

    public void getOrderHistory() {
        interactor.getOrderHistory(new OnFinishListner<OrderHistory>() {
            @Override
            public void onComplete(OrderHistory result) {
                if (getView() != null) {
                    orderHistory = true;
                    getV().hideLoading();
                    getV().setOrderHistory(result);
                    calculateProfit(result);
                    getV().hideEmptyView();
                }
            }

            @Override
            public void onFail(String error) {
                if (getView() != null)
                    getV().hideLoading();
                isDataAvailable();
            }
        });
    }

    public void getOpenOrders(final boolean isRefresh) {
        interactor.getOpenOrder(new OnFinishListner<OpenOrder>() {
            @Override
            public void onComplete(OpenOrder result) {
                if (getView() != null) {
                    openOrder = true;
                    getV().hideLoading();
                    getV().setOpenOrders(result);
                    getV().hideEmptyView();
                    if (isRefresh) {
                        String msg = result.getMessage();
                        if (TextUtils.isEmpty(msg))
                            msg = "Order has been cancled successfully.";
                        CustomDialog.showMessagePop(context, msg, null);
                    }
                }
            }

            @Override
            public void onFail(String error) {
                if (getView() != null)
                    getV().hideLoading();
                isDataAvailable();
            }
        });
    }

    protected void isDataAvailable() {
        if (!openOrder && !orderHistory) {
            if (getView() != null)
                getV().showEmptyView();
        }
    }

    public void cancleOrder(String orderUuid) {
        if (getView() != null)
            getV().showLoading(context.getString(R.string.cancle_msg));
        interactor.cancleOrder(orderUuid, new OnFinishListner<Cancel>() {

            @Override
            public void onComplete(Cancel result) {
                if (result.getSuccess()) {
                    if (getView() != null) {
                        getOpenOrders(true);
                    } else
                        onFail(result.getMessage());
                }
            }

            @Override
            public void onFail(String error) {
                if (getView() != null)
                    getV().hideLoading();
                CustomDialog.showMessagePop(context, error, null);
            }
        });
    }

    private void calculateProfit(OrderHistory history) {
        if (history == null || history.getResult() == null || history.getResult().size() == 0)
            return;
        double sell = 0d, buy = 0d;

        for (Result data : history.getResult()) {
            if (data.getOrderType().toLowerCase().equals("limit_sell") ||
                    data.getOrderType().toLowerCase().equals("conditional_sell")) {
                if (data.getLimit() != null) {
                    if (data.getQuantity() != null)
                        sell += data.getQuantity().doubleValue() * data.getLimit().doubleValue();
                    else
                        sell = data.getLimit().doubleValue();

                }
            } else if (data.getOrderType().toLowerCase().equals("limit_buy") ||
                    data.getOrderType().toLowerCase().equals("conditional_buy")) {
                if (data.getLimit() != null) {
                    if (data.getQuantity() != null)
                        buy += data.getQuantity().doubleValue() * data.getLimit().doubleValue();
                    else
                        buy = data.getLimit().doubleValue();

                }
            }
        }
        double calculation = buy - sell;

        if (getView() != null)
            getV().setCalculation(String.valueOf(GlobalUtil.formatNumber(calculation, "#.########") + "฿"),
                    "$" + String.valueOf(GlobalUtil.formatNumber(GlobalUtil.convertBtcToUsd(calculation), "#.####")));
    }

    public OrderView getV() {
        T view = super.getView();
        if (view == null)
            return null;
        else
            return (OrderView) view;
    }
}

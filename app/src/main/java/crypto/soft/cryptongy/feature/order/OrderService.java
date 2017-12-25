package crypto.soft.cryptongy.feature.order;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import crypto.soft.cryptongy.feature.shared.json.openorder.OpenOrder;
import crypto.soft.cryptongy.feature.shared.json.order.Order;
import crypto.soft.cryptongy.feature.shared.json.order.Result;
import crypto.soft.cryptongy.feature.shared.listner.OnFinishListner;
import crypto.soft.cryptongy.utils.CoinApplication;
import crypto.soft.cryptongy.utils.GlobalUtil;

/**
 * Created by tseringwongelgurung on 12/25/17.
 */

public class OrderService extends IntentService {
    private OrderInteractor interactor = new OrderInteractor();

    public OrderService() {
        super("OrderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startService();
    }

    private void startService() {
        final CoinApplication application = (CoinApplication) getApplication();
        OpenOrder order = application.getOpenOrder();
        if (order == null)
            getOpenOrder(application, true);
        else
            checkOrder(order, application);

    }

    private void getOpenOrder(final CoinApplication application, final boolean check) {
        interactor.getOpenOrder("", application.getReadAccount(), new OnFinishListner<OpenOrder>() {
            @Override
            public void onComplete(OpenOrder result) {
                application.setOpenOrder(result);
                if (check)
                    checkOrder(result, application);
            }

            @Override
            public void onFail(String error) {

            }
        });
    }

    private void checkOrder(OpenOrder openOrder, final CoinApplication application) {
        for (int i = 0; i < openOrder.getResult().size(); i++) {
            final crypto.soft.cryptongy.feature.shared.json.openorder.Result data = openOrder.getResult().get(i);
            final int finalI = i;
            interactor.getOrders(data.getOrderUuid(), application.getReadAccount(), new OnFinishListner<Order>() {

                @Override
                public void onComplete(Order result) {
                    Result order = result.getResult();
                    if (!result.getResult().getIsOpen()) {
                        GlobalUtil.showNotification(OrderService.this, "Order Status", order.getType() + "(" + String.format("%.8f", order.getQuantity().doubleValue()) +
                                ")" + "of " + order.getExchange() + " is filled.", finalI);
                        getOpenOrder(application, false);
                    } else if (result.getResult().getCancelInitiated()) {
                        GlobalUtil.showNotification(OrderService.this, "Order status", order.getType() + "(" + String.format("%.8f", order.getQuantity().doubleValue()) +
                                ")" + "of " + order.getExchange() + " is now cancelled.", finalI);
                        getOpenOrder(application, false);
                    }
                }

                @Override
                public void onFail(String error) {

                }
            });
        }
    }
}
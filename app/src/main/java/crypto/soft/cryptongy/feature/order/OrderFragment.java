package crypto.soft.cryptongy.feature.order;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import crypto.soft.cryptongy.R;
import crypto.soft.cryptongy.feature.shared.json.openorder.OpenOrder;
import crypto.soft.cryptongy.feature.shared.json.openorder.Result;
import crypto.soft.cryptongy.feature.shared.json.orderhistory.OrderHistory;
import crypto.soft.cryptongy.utils.ProgressDialogFactory;

/**
 * Created by tseringwongelgurung on 11/23/17.
 */

public class OrderFragment extends MvpFragment<OrderView, OrderPresenter> implements OrderView,
        View.OnClickListener {
    private View view;

    private TableLayout tblOpenOrders, tblOrderHistory;

    private LinearLayout lnlContainer;
    private TextView txtCalculation, txtLevel, txtOpenOrder, txtOrderHistory, txtEmpty;
    private ImageView imgSync, imgAccSetting;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_order, container, false);
            initToolbar();
            findViews();
            setClickListner();
        }
        return view;
    }

    @Override
    public OrderPresenter createPresenter() {
        return new OrderPresenter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        presenter.getData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ic_menu_order, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.onOptionItemClicked(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initToolbar() {
        TextView txtTitle = getActivity().findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.order);
    }

    @Override
    public void findViews() {
        tblOpenOrders = view.findViewById(R.id.tblOpenOrders);
        tblOrderHistory = view.findViewById(R.id.tblOrderHistory);

        txtCalculation = view.findViewById(R.id.txtCalculation);
        txtLevel = view.findViewById(R.id.txtLevel);

        imgSync = view.findViewById(R.id.imgSync);
        imgAccSetting = view.findViewById(R.id.imgAccSetting);

        txtOpenOrder = view.findViewById(R.id.txtOpen);
        txtOrderHistory = view.findViewById(R.id.txtHistory);

        txtEmpty = view.findViewById(R.id.txtEmpty);
        lnlContainer = view.findViewById(R.id.lnlContainer);
    }

    @Override
    public void setClickListner() {
        imgSync.setOnClickListener(this);
        imgAccSetting.setOnClickListener(this);
    }

    @Override
    public void setCalculation(String calculation) {
        txtCalculation.setText(calculation);
    }

    @Override
    public void setLevel(String level) {
        txtLevel.setText(level);
    }

    @Override
    public void setOpenOrders(OpenOrder openOrders) {
        tblOpenOrders.removeAllViews();
        if (openOrders == null || openOrders.getResult() == null || openOrders.getResult().isEmpty()) {
            txtOpenOrder.setVisibility(View.GONE);
            return;
        }

        txtOpenOrder.setVisibility(View.VISIBLE);
        View title = getLayoutInflater().inflate(R.layout.table_open_order_title, null);
        tblOpenOrders.addView(title);
        for (int i = 0; i < openOrders.getResult().size(); i++) {
            final Result data = openOrders.getResult().get(i);
            View sub = getLayoutInflater().inflate(R.layout.table_open_order_sub, null);
            OpenOrderHolder holder = new OpenOrderHolder(sub);
            holder.txtType.setText(data.getOrderType());
            holder.txtCoin.setText(data.getExchange());
            holder.txtQuantity.setText(String.valueOf(data.getQuantity()));
            holder.txtRate.setText(String.valueOf(data.getPrice()));
            String date = data.getOpened();
            if (!TextUtils.isEmpty(date)) {
                String[] arr = date.split("T");
                String d = arr[0];
                String t = "";
                if (arr.length > 1) {
                    t = arr[1];
                    holder.txtTime.setText(d + "\n" + t);
                } else
                    holder.txtTime.setText(d);
            } else
                holder.txtTime.setText("");
            holder.txtAction.setText("Cancel");
            tblOpenOrders.addView(sub);

            holder.txtAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.cancleOrder(data.getOrderUuid());
                }
            });

            if (i < openOrders.getResult().size() - 1) {
                View line = getLayoutInflater().inflate(R.layout.table_line, null);
                tblOpenOrders.addView(line);
            }
        }
    }

    @Override
    public void setOrderHistory(OrderHistory orderHistory) {
        tblOrderHistory.removeAllViews();
        if (orderHistory == null || orderHistory.getResult() == null || orderHistory.getResult().isEmpty()) {
            txtOrderHistory.setVisibility(View.GONE);
            return;
        }

        txtOrderHistory.setVisibility(View.VISIBLE);
        View title = getLayoutInflater().inflate(R.layout.table_order_history_title, null);
        tblOrderHistory.addView(title);
        for (int i = 0; i < orderHistory.getResult().size(); i++) {
            crypto.soft.cryptongy.feature.shared.json.orderhistory.Result data = orderHistory.getResult().get(i);
            View sub = getLayoutInflater().inflate(R.layout.talbe_order_history_sub, null);
            OrderHistoryHolder holder = new OrderHistoryHolder(sub);
            holder.txtType.setText(data.getOrderType());
            holder.txtQuantity.setText(String.valueOf(data.getQuantity()));
            holder.txtRate.setText(String.valueOf(data.getPrice()));
            String date = data.getTimeStamp();
            if (!TextUtils.isEmpty(date)) {
                String[] arr = date.split("T");
                String d = arr[0];
                String t = "";
                if (arr.length > 1) {
                    t = arr[1];
                    holder.txtTime.setText(d + "\n" + t);
                } else
                    holder.txtTime.setText(d);
            } else
                holder.txtTime.setText("");
            tblOrderHistory.addView(sub);

            if (i < orderHistory.getResult().size() - 1) {
                View line = getLayoutInflater().inflate(R.layout.table_line, null);
                tblOrderHistory.addView(line);
            }
        }
    }

    @Override
    public void showLoading(String msg) {
        ProgressDialogFactory.getInstance(getContext(), msg).show();
    }

    @Override
    public void hideLoading() {
        ProgressDialogFactory.dismiss();
    }

    @Override
    public void showEmptyView() {
        txtEmpty.setVisibility(View.VISIBLE);
        lnlContainer.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyView() {
        txtEmpty.setVisibility(View.GONE);
        lnlContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        presenter.onClicked(id);
    }
}

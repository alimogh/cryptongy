package crypto.soft.cryptongy.feature.home;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import crypto.soft.cryptongy.R;
import crypto.soft.cryptongy.feature.shared.json.market.Result;
import crypto.soft.cryptongy.feature.shared.listner.AdapterItemClickListener;

/**
 * Created by Ajahar on 11/21/2017.
 */

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> implements Filterable {
    List<Result> currencyItems;
    List<Result> currencyItemsFiltered;
    private SparseBooleanArray mSelectedItemsIds;
    private AdapterItemClickListener adapterItemClickListener;


    public CurrencyAdapter(List<Result> currencyItems) {
        this.currencyItems = currencyItems;
        this.currencyItemsFiltered = currencyItems;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    static int getProgress(double a, double b) {
        return (int) ((a * 100) / b);
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CurrencyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, int position) {
        Result result = currencyItemsFiltered.get(position);
        holder.coin.setText(result.getMarketName());
        holder.volume.setText("" + result.getVolume());
        holder.low.setText("" + result.getLow());
        holder.high.setText("" + result.getHigh());
        double s = Double.parseDouble("" + result.getLast());
        holder.price.setText(String.format("%.8f", s));

        if (!mSelectedItemsIds.get(position)) {
            holder.parent.setBackgroundResource(R.drawable.rect);
        } else {
            holder.parent.setBackgroundResource(R.drawable.rect_fill_selected);
        }
        // holder.seekBar.setProgress(50);
        holder.seekBar.setEnabled(false);
        // holder.seekBar.setProgress(getProgress(round(Double.parseDouble(holder.price.getText().toString()),2),round(Double.parseDouble(holder.high.getText().toString()),2)));
        holder.seekBar.setProgress(50);
        holder.seekBar.setOnSeekBarChangeListener(new OnSeekBarChange(position, holder, currencyItemsFiltered.get(position)));
        holder.parent.setOnLongClickListener(new OnCurrencyItemClick(position, holder, currencyItemsFiltered.get(position)));

    }

    @Override
    public int getItemCount() {
        return currencyItemsFiltered.size();
    }

    public void setAdapterItemClickListener(AdapterItemClickListener adapterItemClickListener) {
        this.adapterItemClickListener = adapterItemClickListener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    currencyItemsFiltered = currencyItems;
                } else {
                    List<Result> filteredList = new ArrayList<>();
                    for (Result row : currencyItems) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getMarketName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    currencyItemsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = currencyItemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                currencyItemsFiltered = (ArrayList<Result>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public void setmSelectedItemsIds() {
        mSelectedItemsIds.clear();
    }

    public static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.coin)
        TextView coin;
        @BindView(R.id.volume)
        TextView volume;
        @BindView(R.id.seekBar)
        SeekBar seekBar;
        @BindView(R.id.parent)
        LinearLayout parent;
        @BindView(R.id.low)
        TextView low;
        @BindView(R.id.price)
        TextView price;
        @BindView(R.id.high)
        TextView high;

        public CurrencyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class OnCurrencyItemClick implements View.OnLongClickListener {
        int position;
        CurrencyViewHolder currencyViewHolder;
        Result item;

        public OnCurrencyItemClick(int position, CurrencyViewHolder currencyViewHolder, Result item) {
            this.position = position;
            this.currencyViewHolder = currencyViewHolder;
            this.item = item;
        }


        @Override
        public boolean onLongClick(View view) {
            if (adapterItemClickListener != null) {
                if (!mSelectedItemsIds.get(position)) {
                    mSelectedItemsIds.put(position, true);
                    currencyViewHolder.parent.setBackgroundResource(R.drawable.rect_fill_selected);
                } else {
                    mSelectedItemsIds.delete(position);
                    currencyViewHolder.parent.setBackgroundResource(R.drawable.rect);
                }
                adapterItemClickListener.onItemLongClicked(item, position);
            }
            return true;
        }
    }

    class OnSeekBarChange implements SeekBar.OnSeekBarChangeListener {
        int position;
        CurrencyViewHolder currencyViewHolder;
        Result item;
        int progressChangedValue = 0;

        public OnSeekBarChange(int position, CurrencyViewHolder currencyViewHolder, Result item) {
            this.position = position;
            this.currencyViewHolder = currencyViewHolder;
            this.item = item;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            progressChangedValue = i;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.v("progress", "" + progressChangedValue);
        }
    }
}
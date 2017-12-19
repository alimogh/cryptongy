package crypto.soft.cryptongy.feature.shared.ticker;

import android.os.AsyncTask;

import java.io.IOException;

import crypto.soft.cryptongy.feature.shared.json.ticker.Ticker;
import crypto.soft.cryptongy.feature.shared.listner.OnFinishListner;
import crypto.soft.cryptongy.network.BittrexServices;

/**
 * Created by tseringwongelgurung on 12/18/17.
 */

public class TickerInteractor {
    public void getTicker(final String coinName, final OnFinishListner<Ticker> listner) {

        new AsyncTask<Void, Void, Ticker>() {

            @Override
            protected Ticker doInBackground(Void... voids) {
                try {
                    return new BittrexServices().getTicker(coinName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Ticker ticker) {
                super.onPostExecute(ticker);
                if (ticker == null)
                    listner.onFail("Failed to fetch data");
                else if (ticker.getSuccess().booleanValue())
                    listner.onComplete(ticker);
                else
                    listner.onFail(ticker.getMessage());
            }
        }.execute();
    }
}
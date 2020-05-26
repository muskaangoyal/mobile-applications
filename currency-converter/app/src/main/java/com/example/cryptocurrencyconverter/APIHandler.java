package com.example.cryptocurrencyconverter;
import android.renderscript.ScriptGroup;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class APIHandler {

    protected static void apiCall() {
        /**
         * Source: https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
         * Learn about applying APIs through get requests, I referenced HyLian's answer
         */
        try {
            String urlString = "http://www.api.currencylayer.com/live?access_key=25a3c41d17bde9d4301f3e1d1ad5451d&source=USD&currencies=BTC,GBP&format=1";
            URL url = new URL(urlString);
            URLConnection urlConn = url.openConnection();
            InputStream input = urlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(input);
            JsonReader json = new JsonReader(inputStreamReader);
            json.beginArray();
            while(json.hasNext()) {
                json.beginObject();
                String quotes = json.nextName();
                if(quotes.equals("quotes")) {
                    while (json.hasNext()) {
                        String USDBTC = json.nextName();
                        if (USDBTC.equals("USDBTC")) {
                            MainActivity.bitcoinToDollarRatio = 1 / (json.nextDouble());
                            break;
                        } else {
                            json.skipValue();
                        }

                    }
                }
                json.endObject();
            }

        } catch (Error e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return;
        }
    }

}

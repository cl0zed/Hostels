package com.example.hotels;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Hotel> hotels = null;
    private LinearLayout downloadLayout;
    private Toolbar toolbar;
    private ListView listView;
    private HotelAdapter adapter;
    private static final String hotelsList = "hotelList";
    public static final String MY_PREF = "myPref";
    private static final String hotelsInfoURL =  "https://dl.dropboxusercontent.com/u/109052005/1/0777.json";
    private static final String [] listDownloadStatus = {"start", "update"};
    private SharedPreferences listCache;
    private boolean isDistance = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);

        downloadLayout = (LinearLayout) findViewById(R.id.downloadLayout);
        listView = (ListView) findViewById(R.id.listview);

        listCache = getSharedPreferences(MY_PREF, 0);



        new HotelListDownloader().execute(hotelsInfoURL, listDownloadStatus[0]);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.distanceSorted).setChecked(isDistance);
        menu.findItem(R.id.suitedSorted).setChecked(!isDistance);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public void alertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.no_internet_content_message)
                .setCancelable(false)
                .setTitle(R.string.no_internet_title_message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("isDistance", isDistance);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isDistance = savedInstanceState.getBoolean("isDistance");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.distanceSorted:
                item.setChecked(true);
                //if (item.isChecked()){
                    Collections.sort(hotels);
                    isDistance = true;
                    adapter = new HotelAdapter(this, hotels);
                    listView.setAdapter(adapter);
                //}
                break;
            case R.id.suitedSorted:
                item.setChecked(true);
                //item.setChecked(!item.isChecked());
                //if (item.isChecked()){
                    Collections.sort(hotels, new SuitesComparator());
                    isDistance = false;
                    adapter = new HotelAdapter(this, hotels);
                    listView.setAdapter(adapter);
                //}

                break;
            case R.id.updateList:
                listView.setAdapter(null);
                downloadLayout.setVisibility(View.VISIBLE);
                new HotelListDownloader().execute(hotelsInfoURL, listDownloadStatus[1]);
                break;

            case R.id.exitBtn:
                System.exit(0);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void getHotelList(String jsonString)
    {
        hotels = new ArrayList<>();
        try{

            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); ++i)
            {
                Hotel hotel = new Hotel(jsonArray.getJSONObject(i));
                hotels.add(hotel);
            }
        }catch (JSONException e)
        {
            Log.e("JSON", e.getMessage());
        }
    }

    public void addGUI(){
        downloadLayout.setVisibility(View.INVISIBLE);
        if (!isDistance)
            Collections.sort(hotels, new SuitesComparator());
        else Collections.sort(hotels);
        adapter = new HotelAdapter(this, hotels);
        listView.setAdapter(adapter);
    }

    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy(){
        super.onDestroy();
        hotels = null;
    }

    private boolean isOnline()
    {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    class HotelListDownloader extends AsyncTask<String, Void, String> {
        protected String doInBackground(String[] params)
        {
            if (params[1].equals(listDownloadStatus[0])) {
                String listResult = listCache.getString(hotelsList, "");
                if (!listResult.equals("")){
                    getHotelList(listResult);
                    return "";
                }
            }
            HttpURLConnection urlConnection;
            BufferedReader reader;
            String result = "";
            try {
                if (!isOnline()) return null;
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(150000);

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line);
                }
                urlConnection.disconnect();
                result = buffer.toString();

            } catch(MalformedURLException e)
            {
                Log.e("Error", "Error in url");

            } catch (IOException e)
            {
                Log.e("Error", "Error in open connection");
            }

            SharedPreferences.Editor editor = listCache.edit();
            editor.putString(hotelsList, result);
            editor.apply();

            getHotelList(result);

            return result;
        }

        protected void onPostExecute(String json)
        {
            if (json == null) {
                alertMessage();
                return;
            }
            super.onPostExecute(json);
            addGUI();

        }
    }

}

package com.example.hotels;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InformationActivity extends AppCompatActivity {

    LinearLayout downloadLayout;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences(MainActivity.MY_PREF, 0);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        downloadLayout = (LinearLayout) findViewById(R.id.downloadInfoLayout);

        Intent intent = getIntent();

        String url = "https://dl.dropboxusercontent.com/u/109052005/1/";

        if (isOnline()){
            new HotelInfoDownloader().execute(url, intent.getIntExtra("id", 0) + ".json");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage(R.string.no_internet_content_message)
                    .setCancelable(false)
                    .setTitle(R.string.no_internet_title_message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private Hotel createInfoHotel(String json)
    {
        Hotel hotel = null;
        try{

            JSONObject jsonObject = new JSONObject(json);
            hotel = new Hotel(jsonObject);
            hotel.setAdditionalInformation(jsonObject);
        }catch (JSONException e)
        {
            Log.e("JSON", e.getMessage());
        }
        return hotel;
    }



    private void setNoInformationGUI()
    {
        downloadLayout.setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.hotel_name)).setText(R.string.no_hotel_information);
    }
    private void setGUI(Hotel hotel){

        downloadLayout.setVisibility(View.INVISIBLE);
        try {
            TextView hotelName = (TextView) findViewById(R.id.hotel_name);
            TextView hotelInfo = (TextView) findViewById(R.id.hotel_info);

            hotelName.setText(hotel.getName());
            String textColor = Integer.toHexString(getResources().getColor(R.color.colorTextInfo) & 0x00ffffff);
            hotelInfo.setText(Html.fromHtml(getHtmlInfo(hotel, textColor)));

            findViewById(R.id.download_photo_info).setVisibility(View.VISIBLE);
            findViewById(R.id.photo_download_progress).setVisibility(View.VISIBLE);

            String imgURL = "https://dl.dropboxusercontent.com/u/109052005/1/" + hotel.getImg();

            new ImageDownloader().execute(imgURL, hotel.getImg());
        } catch (NullPointerException e)
        {
            Log.e("Error", "Hotel info is empty");
        }
    }

    private void setImage(Bitmap bitmap, final String name)
    {
        findViewById(R.id.photo_download_progress).setVisibility(View.GONE);
        if (bitmap == null)
        {
            ((TextView) findViewById(R.id.download_photo_info)).setText(R.string.no_photos);
        } else{

            ((TextView) findViewById(R.id.download_photo_info)).setText(R.string.photos);
            bitmap = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth() - 4, bitmap.getHeight() - 4);
            ImageView view = (ImageView) findViewById(R.id.id_hotel_image);
            view.setImageBitmap(bitmap);

            File cacheFile = new File(getBaseContext().getCacheDir(), name);
            try{

                FileOutputStream out = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

            } catch (IOException e)
            {
                Log.e("Error", e.getMessage());
            }


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InformationActivity.this, FullScreenImage.class);
                    intent.putExtra("Name", name);
                    startActivity(intent);
                }
            });
        }

    }

    private String getHtmlInfo(Hotel hotel, String textColor)
    {
        return String.format("<big><font color = #"+ textColor + "><b>Address: </b></big>%s"
                +"<br><big><font color = #"+ textColor + "><b>Stars: </b></font></big>%s"
                +"<br><big><font color = #"+ textColor + "><b>Distance: </b></font></big>%s"
                +"<br><big><font color = #"+ textColor + "><b>Available suites: </b></font></big>%s"
                +"<br><big><font color = #"+ textColor + "><b>Lon: </b></font></big>%s"
                +"<br><big><font color = #"+ textColor + "><b>Lat: </b></font></big>%s<br>",
                hotel.getAddress(), hotel.getStars(), hotel.getDistance(), hotel.getAvailability(),
                hotel.getLon(), hotel.getLat());
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

    class HotelInfoDownloader extends AsyncTask<String, Void, Hotel>
    {

        @Override
        protected Hotel doInBackground(String[] params)
        {
            Hotel hotel;
            String hotelInfo = preferences.getString(params[1], "");
            if (!hotelInfo.equals("")){
                hotel = createInfoHotel(hotelInfo);
                return hotel;
            }
            HttpURLConnection urlConnection;
            BufferedReader reader;
            String result = "";

            try {
                URL url = new URL(params[0] + params[1]);


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);

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
            SharedPreferences.Editor editor = preferences.edit();
            if (result.equals("")) result = "No information";
            editor.putString(params[1], result);
            editor.apply();

            hotel = createInfoHotel(result);

            return hotel;
        }

        @Override
        protected void onPostExecute(Hotel hotel)
        {
            super.onPostExecute(hotel);
            if (hotel == null) setNoInformationGUI(); else setGUI(hotel);
        }
    }
    class ImageDownloader extends AsyncTask<String, Void, Bitmap>
    {
        private String name;
        @Override
        protected Bitmap doInBackground(String[] params)
        {
            Bitmap bitmap;
            name = params[1];

            File picture = new File(getBaseContext().getCacheDir(), name);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(picture);
            }catch (FileNotFoundException e)
            {
                Log.e("No file", e.getMessage() + " name: " + name);
            }

            bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) return bitmap;

            try {
                URL url = new URL(params[0]);

                InputStream input = url.openStream();
                bitmap = BitmapFactory.decodeStream(input);

                bitmap = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth() - 2, bitmap.getHeight() - 2);

            } catch(MalformedURLException e)
            {
                Log.e("Error", "Error in url");
            } catch (IOException e)
            {
                Log.e("Error", "Error in open connection");
            }


            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            setImage(bitmap, name);
        }
    }
}

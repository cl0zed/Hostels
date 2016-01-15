package com.example.hotels;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.ArrayList;


public class HotelAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Hotel> hotels;

    HotelAdapter(Context context, ArrayList<Hotel> objects)
    {
        this.context = context;
        this.hotels = objects;
    }

    static class ViewHolder{
        TextView nameText;
        TextView addressText;
        TextView distanceText;
        TextView starsText;
        TextView suitesText;
        ImageButton imageButton;
    }
    public int getCount()
    {
        return hotels.size();
    }

    public Hotel getItem(int position)
    {
        return hotels.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if (convertView == null)
        {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_hotel, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.nameText = (TextView)convertView.findViewById(R.id.nameTextView);
            viewHolder.addressText = (TextView)convertView.findViewById(R.id.addressTextView);
            viewHolder.distanceText = (TextView)convertView.findViewById(R.id.distanceTextView);
            viewHolder.starsText = (TextView)convertView.findViewById(R.id.starsTextView);
            viewHolder.suitesText = (TextView)convertView.findViewById(R.id.availabilitySuites);
            viewHolder.imageButton = (ImageButton) convertView.findViewById(R.id.getFullInformation);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Hotel hotel = hotels.get(position);
        String address = "Address:" + hotel.getPreviewAddress();
        String distance = "From city's center: " + hotel.getDistance();
        String stars = "Stars: " + hotel.getStars();
        String availableSites = "Available suites: " + hotel.getSuitesCount();

        viewHolder.nameText.setText(hotel.getShortName());
        viewHolder.addressText.setText(address);
        viewHolder.distanceText.setText(distance);
        viewHolder.starsText.setText(stars);
        viewHolder.suitesText.setText(availableSites);


        viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, InformationActivity.class);
                intent.putExtra("id", hotel.getId());
                context.startActivity(intent);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, InformationActivity.class);
                intent.putExtra("id", hotel.getId());
                context.startActivity(intent);
            }
        });
        return convertView;
    }



}

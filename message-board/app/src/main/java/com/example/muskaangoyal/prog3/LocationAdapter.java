package com.example.muskaangoyal.prog3;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muskaangoyal.prog3.R;

import java.util.ArrayList;

import static com.example.muskaangoyal.prog3.LocationAdapter.mContext;

public class LocationAdapter extends RecyclerView.Adapter {

    public static Context mContext;
    private ArrayList<com.example.muskaangoyal.prog3.Bears> mBears;

    public LocationAdapter(Context context, ArrayList<com.example.muskaangoyal.prog3.Bears> bears) {
        mContext = context;
        mBears = bears;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bear_cell_layout, parent, false);
        return new BearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        com.example.muskaangoyal.prog3.Bears bear = mBears.get(position);
        ((BearViewHolder) holder).bind(bear);
    }
    @Override
    public int getItemCount() {
        return mBears.size();
    }
}

class BearViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout mBearBubbleLayout;
    public ImageView mLocationPictureImageView;
    public TextView mLocationNameTextView;
    public TextView mDistanceTextView;

    public BearViewHolder(View itemView) {
        super(itemView);
        mBearBubbleLayout = itemView.findViewById(R.id.bear_cell_layout);
        mLocationPictureImageView = mBearBubbleLayout.findViewById(R.id.location_picture);
        mLocationNameTextView = mBearBubbleLayout.findViewById(R.id.location_name);
        mDistanceTextView = mBearBubbleLayout.findViewById(R.id.distance_info);

        mBearBubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDistanceTextView.getText() == "less than 10 meters away") {
                    Context oContext = mContext;
                    Intent i = new Intent(oContext, com.example.muskaangoyal.prog3.CommentFeedActivity.class);
                    i.putExtra("username", com.example.muskaangoyal.prog3.LocationActivity.username);
                    i.putExtra("locationName", mLocationNameTextView.getText());
                    oContext.startActivity(i);
                } else {
                    Toast.makeText(mContext, "You must be within 10 meters of a landmark to access", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void bind(com.example.muskaangoyal.prog3.Bears bear) {
        mLocationPictureImageView.setImageResource(bear.landmarkPicture);
        mLocationNameTextView.setText(bear.landmarkName);
        mDistanceTextView.setText(bear.coordinates);
    }
}

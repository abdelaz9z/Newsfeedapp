package com.example.newsfeedapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsAdapter extends ArrayAdapter<News> {

    Group group;

    NewsAdapter(@NonNull Context context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_itme_news, parent, false);
        }

        News currentNews = getItem(position);
        group = convertView.findViewById(R.id.group);

        // Get and display the article's thumbnail
        ImageView thumbnailImage = convertView.findViewById(R.id.image_thumbnail);
        assert currentNews != null;
        Bitmap thumbnail = currentNews.getThumbnail();
        if (thumbnail != null) {
            thumbnailImage.setImageBitmap(currentNews.getThumbnail());
        } else {
            group.setVisibility(View.INVISIBLE);

        }

        // Get and display the article's web title
        TextView webTitleText = convertView.findViewById(R.id.text_web_title);
        webTitleText.setText(currentNews.getWebTitle());

        // Get and display the article's trail text
        TextView trailText = convertView.findViewById(R.id.text_trail_text);
        trailText.setText(currentNews.getTrailText());

        // Get and display the article's byline
        TextView byline = convertView.findViewById(R.id.text_byline);
        byline.setText(currentNews.getByline());

        // Get and display the article's section name
        TextView sectionName = convertView.findViewById(R.id.text_section_name);
        sectionName.setText(currentNews.getSectionName());


        // Get and display the article's web publication date
        TextView webPublicationDate = convertView.findViewById(R.id.text_web_publication_date);
        webPublicationDate.setText(formatDateString(currentNews.getWebPublicationDate()));

        // Get and display the article's web publication time
        TextView webPublicationTime = convertView.findViewById(R.id.text_web_publication_time);
        webPublicationTime.setText(formatTimeString(currentNews.getWebPublicationDate()));

        return convertView;
    }

    /**
     * Return a formatted time string (i.e. "Mar 3, '18") from a Date object.
     */
    private String formatDateString(String date) {
        final SimpleDateFormat inputParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

        Date dateOut = null;
        try {
            dateOut = inputParser.parse(date);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final SimpleDateFormat outputFormatter = new SimpleDateFormat("MMM dd ''yy", Locale.US);
        assert dateOut != null;
        return outputFormatter.format(dateOut);
    }

    /**
     * Return a formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTimeString(String date) {
        final SimpleDateFormat inputParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

        Date dateOut = null;
        try {
            dateOut = inputParser.parse(date);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final SimpleDateFormat outputFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        assert dateOut != null;
        return outputFormatter.format(dateOut);
    }
}

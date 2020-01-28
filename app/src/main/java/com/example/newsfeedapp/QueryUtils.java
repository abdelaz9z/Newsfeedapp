package com.example.newsfeedapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    public QueryUtils() {
    }

    /**
     * Query the USGS dataset and return a list of {@link News} objects.
     */
    static List<News> fetchNewsData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        // Return the list of {@link Book}s
        return extractFeatureFromJson(jsonResponse);
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<News> extractFeatureFromJson(String newsJSON) {

        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        List<News> newsList = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(newsJSON);
            JSONObject responseJsonObject = baseJsonResponse.getJSONObject("response");
            JSONArray resultsJsonArray = responseJsonObject.getJSONArray("results");
            for (int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject currentNews = resultsJsonArray.getJSONObject(i);
                JSONObject fieldsJsonObject = currentNews.getJSONObject("fields");

                // Section name
                String sectionName = currentNews.getString("sectionName");

                // Web publication date
                String webPublicationDate = currentNews.getString("webPublicationDate");

                // Web title
                String webTitle = currentNews.getString("webTitle");

                // Web url
                String webUrl = currentNews.getString("webUrl");

                // Trail text
                String trailText = fieldsJsonObject.getString("trailText");

                // Byline
                String byline;
                if (!fieldsJsonObject.isNull("byline")) {
                    byline = fieldsJsonObject.getString("byline");
                } else {
                    byline = "Unknown author";
                }


                // Thumbnail
                String thumbnail;
                Bitmap thumbnailBitmap;
                if (!fieldsJsonObject.isNull("thumbnail")) {
                    thumbnail = fieldsJsonObject.getString("thumbnail");
                    thumbnailBitmap = downloadBitmap(thumbnail);
                } else {
                    thumbnailBitmap = null;
                }

                News news = new News(thumbnailBitmap, webTitle, trailText, byline, sectionName, webPublicationDate, webUrl);
                newsList.add(news);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the books JSON results", e);
        }

        // Return the list of books
        return newsList;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Load the low res thumbnail image from the URL. If available in 1000px format,
     * use that instead. Return a {@link Bitmap}
     * Credit to Mohammad Ali Fouani via https://stackoverflow.com/q/51587354/9302422
     *
     * @param originalUrl string of the original URL link to the thumbnail image
     * @return Bitmap of the image
     */
    private static Bitmap downloadBitmap(String originalUrl) {
        Bitmap bitmap = null;
        // If thumbnail exists, replace the end of the originalUrl into a newUrl string
        // (e.g. /500.jpg or similar) with /1000.jpg
        if (!"".equals(originalUrl)) {
            String newUrl = originalUrl.replace
                    (originalUrl.substring(originalUrl.lastIndexOf("/")), "/1000.jpg");
            try {
                // see if the higher-res image exists
                InputStream inputStream = new URL(newUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                try {
                    // if no higher-res image is found, revert to original image url
                    InputStream inputStream = new URL(originalUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (Exception ignored) {
                }
            }
        }
        return bitmap;
    }

}
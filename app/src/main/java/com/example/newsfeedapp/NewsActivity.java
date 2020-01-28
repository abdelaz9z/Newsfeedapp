package com.example.newsfeedapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

import android.app.LoaderManager.LoaderCallbacks;
import android.widget.Toast;

import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {

    private static final String URL_REQUEST_NEWS = "https://content.guardianapis.com/search";
    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter mNewsAdapter;
    private TextView mEmptyText;
    private TextView mTopicText;
    private String mTopic;
    private ConnectivityManager mConnectivityManager;
    private SwipeRefreshLayout mSwipeContainer;

    String mGetSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Lookup the swipe container view
        mSwipeContainer = findViewById(R.id.swipeContainer);
        mSwipeContainer.setRefreshing(true);

        mTopicText = findViewById(R.id.text_topic);

        ListView newsListView = findViewById(R.id.list_news);
        mEmptyText = findViewById(R.id.text_empty);
        newsListView.setEmptyView(mEmptyText);

        mNewsAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(mNewsAdapter);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // If there is a network connection, fetch data
        loadData();

        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Check for internet connection and attempt to load data
                loadData();
            }
        });

        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(
                R.color.colorByline,
                R.color.colorBookmark);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News currentNews = mNewsAdapter.getItem(position);
                assert currentNews != null;
                if (currentNews.getWebUrl() != null) {
                    Uri newsUri = Uri.parse(currentNews.getWebUrl());
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                    startActivity(websiteIntent);
                } else {
                    Toast.makeText(NewsActivity.this, "Missing info of web url", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search here!");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mGetSearch = searchView.getQuery().toString();
                getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsActivity.this);
                mTopicText.setText(mTopic);
                mSwipeContainer.setRefreshing(true);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.app_bar_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.app_bar_refresh:
                mSwipeContainer.setRefreshing(true);
                loadData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pageSize = sharedPreferences.getString(getString(R.string.settings_page_size_key), getString(R.string.settings_page_size_default));
        String orderBy = sharedPreferences.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        mTopic = sharedPreferences.getString(getString(R.string.settings_topic_key), getString(R.string.settings_topic_default));

        Uri baseUri = Uri.parse(URL_REQUEST_NEWS);
        Uri.Builder builder = baseUri.buildUpon();

        builder.appendQueryParameter(getString(R.string.settings_api_key_key), getString(R.string.settings_api_key_default));
        builder.appendQueryParameter(getString(R.string.settings_page_size_key), pageSize);
        builder.appendQueryParameter(getString(R.string.settings_order_by_key), orderBy);
        builder.appendQueryParameter(getString(R.string.settings_show_fields_key), getString(R.string.settings_show_fields_default));

        if (mGetSearch != null && !mGetSearch.isEmpty()) {
            mTopic = mGetSearch;
            builder.appendQueryParameter(getString(R.string.settings_topic_key), mTopic);
        } else {
            builder.appendQueryParameter(getString(R.string.settings_topic_key), mTopic);
        }

        return new NewsLoader(this, builder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Hide swipe to reload spinner
        mSwipeContainer.setRefreshing(false);

        mNewsAdapter.clear();
        if (news != null && !news.isEmpty()) {
            mNewsAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mNewsAdapter.clear();
    }

    public boolean checkConnection(ConnectivityManager connectivityManager) {
        // Status of internet connection
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Loads and reloads the data as requested
     */
    public void loadData() {
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert mConnectivityManager != null;
        boolean isConnected = checkConnection(mConnectivityManager);
        if (isConnected) {
            getLoaderManager().destroyLoader(NEWS_LOADER_ID);
            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
            mEmptyText.setText(R.string.msg_no_news);
            mTopicText.setText(mTopic);
            mSwipeContainer.setRefreshing(true);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mSwipeContainer.setRefreshing(false);
            mEmptyText.setText(R.string.msg_no_internet_connection);
            mNewsAdapter.clear();
        }
    }
}

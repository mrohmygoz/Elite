package com.uninum.elite.utility;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {

    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private Context context;
    public EndlessScrollListener(Context context) {
    	this.context = context;
    }
    public EndlessScrollListener(int visibleThreshold, Context context) {
        this.visibleThreshold = visibleThreshold;
        this.context = context;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of gigs using a background task,
            // but you can call any function here.
//            new LoadGigsTask().execute(currentPage + 1);
        	Toast.makeText(context, "End", Toast.LENGTH_SHORT).show();
            loading = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
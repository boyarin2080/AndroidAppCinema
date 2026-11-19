package com.example.onlinecinema;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Shows the selected movie page inside a WebView.
 */
public class PlayActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String movieId;
    private int fallbackStage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.playProgress);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                    loadFallbackPage();
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            android.webkit.WebResourceResponse errorResponse) {
                if (request.isForMainFrame() && errorResponse.getStatusCode() >= 400) {
                    progressBar.setVisibility(View.GONE);
                    loadFallbackPage();
                }
            }
        });

        String extraId = getIntent().getStringExtra("id");
        movieId = extraId == null ? "" : extraId.replace(".0", "");

        // Android 16+ uses predictive back, so onBackPressed() is not called anymore.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        String url = "https://reyohoho.github.io/reyohoho/#/movie/" + movieId;
        webView.loadUrl(url);
    }

    /**
     * The site used in the manual has moved. If the old address is unavailable,
     * try the current address of the same catalog.
     */
    private void loadFallbackPage() {
        if (fallbackStage == 0) {
            fallbackStage = 1;
            webView.loadUrl("https://www.kinopoisk.ru/film/" + movieId + "/");
        } else {
            Toast.makeText(this, R.string.player_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

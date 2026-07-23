package com.dotorbits.elengaschool;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://sm.dotorbits.com/";
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout offlineView;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildInterface();
        configureWebView();
        if (savedInstanceState == null) loadHome(); else webView.restoreState(savedInstanceState);
    }

    private void buildInterface() {
        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(-1, 8);
        progressParams.gravity = Gravity.TOP;
        root.addView(progressBar, progressParams);

        offlineView = new LinearLayout(this);
        offlineView.setOrientation(LinearLayout.VERTICAL);
        offlineView.setGravity(Gravity.CENTER);
        offlineView.setPadding(48, 48, 48, 48);
        offlineView.setBackgroundColor(Color.WHITE);
        TextView title = new TextView(this);
        title.setText(R.string.offline_title);
        title.setTextSize(22);
        title.setTextColor(Color.rgb(6, 59, 61));
        TextView message = new TextView(this);
        message.setText(R.string.offline_message);
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 16, 0, 28);
        Button retry = new Button(this);
        retry.setText(R.string.retry);
        retry.setOnClickListener(v -> loadHome());
        offlineView.addView(title);
        offlineView.addView(message);
        offlineView.addView(retry);
        offlineView.setVisibility(View.GONE);
        root.addView(offlineView, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString(settings.getUserAgentString() + " ElengaSchoolAndroid/1.0");

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("https".equals(scheme) || "http".equals(scheme)) {
                    if ("sm.dotorbits.com".equals(uri.getHost())) return false;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception ignored) { Toast.makeText(MainActivity.this, "Unable to open link", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                CookieManager.getInstance().flush();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                progressBar.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent chooser = params.createIntent();
                chooser.setType("*/*");
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent combined = Intent.createChooser(chooser, "Select file");
                combined.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{camera});
                startActivityForResult(combined, FILE_CHOOSER_REQUEST);
                return true;
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, length) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
            request.addRequestHeader("User-Agent", userAgent);
            request.setMimeType(mimeType);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "ElengaSchool_" + System.currentTimeMillis() + (extension == null ? "" : "." + extension));
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = manager.getActiveNetwork();
        return network != null;
    }

    private void loadHome() {
        if (isOnline()) {
            offlineView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(HOME_URL);
        } else {
            webView.setVisibility(View.GONE);
            offlineView.setVisibility(View.VISIBLE);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileCallback != null) {
            Uri[] result = resultCode == RESULT_OK ? WebChromeClient.FileChooserParams.parseResult(resultCode, data) : null;
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}

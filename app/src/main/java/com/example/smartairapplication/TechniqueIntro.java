package com.example.smartairapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TechniqueIntro extends AppCompatActivity {

    private ImageView btnBack;
    private FrameLayout videoCard;
    private ImageView imgThumbnail;
    private ImageView imgPlayOverlay;
    private WebView webViewVideo;
    private Button btnStartPractice;
    private static final String YOUTUBE_VIDEO_ID = "G3z1dClBJxI";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_technique_intro);

        btnBack = findViewById(R.id.btnBack);
        videoCard = findViewById(R.id.videoCard);
        imgThumbnail = findViewById(R.id.imgVideoThumbnail);
        imgPlayOverlay =findViewById(R.id.imgPlayOverlay);
        webViewVideo = findViewById(R.id.webViewVideo);
        btnStartPractice = findViewById(R.id.btnStartPractice);

        //back to previous screen
        btnBack.setOnClickListener(v -> finish());

        //setup webview (but don't load vid yet)
        WebSettings webSettings = webViewVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewVideo.setWebViewClient(new WebViewClient());

        //only load vid when user taps thumbnail / play
        View.OnClickListener playClickListener = v -> {
            videoCard.setVisibility(View.GONE);
            webViewVideo.setVisibility(View.VISIBLE);

            String html = "<html><body style='margin:0;padding:0;'>"
                    + "<iframe width='100%' height='100%' "
                    + "src='https://www.youtube.com/embed/" + YOUTUBE_VIDEO_ID + "?autoplay=1' "
                    + "frameborder='0' allow='autoplay; encrypted-media' allowfullscreen>"
                    + "</iframe></body></html>";

            webViewVideo.loadData(html, "text/html", "utf-8");
        };

        videoCard.setOnClickListener(playClickListener);
        imgThumbnail.setOnClickListener(playClickListener);
        imgPlayOverlay.setOnClickListener(playClickListener);

        // start step-by-step practice
        btnStartPractice.setOnClickListener(v -> {
            startActivity(new android.content.Intent(
                    TechniqueIntro.this,
                    TechniqueHelper.class
            ));
        });

    }

    @Override
    protected void onDestroy() {
        if (webViewVideo != null) {
            webViewVideo.loadUrl("about:blank");
            webViewVideo.destroy();
        }
        super.onDestroy();
    }
}
package com.shadowx.panel

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var floatingBall: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.webViewClient = WebViewClient()

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        floatingBall = TextView(this)
        floatingBall.text = "SHADOW\nX"
        floatingBall.textSize = 12f
        floatingBall.setTextColor(Color.WHITE)
        floatingBall.setGravity(Gravity.CENTER)
        floatingBall.setBackgroundColor(Color.rgb(120, 0, 0))
        floatingBall.elevation = 20f

        val ballParams = FrameLayout.LayoutParams(
            dp(64),
            dp(64)
        )

        ballParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        ballParams.setMargins(0, 0, dp(12), 0)

        root.addView(floatingBall, ballParams)

        floatingBall.setOnClickListener {
            floatingBall.visibility = View.GONE
        }

        setContentView(root)

        webView.loadUrl(
            "https://www.82winoo.com/#/register?invitationCode=782544845183"
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

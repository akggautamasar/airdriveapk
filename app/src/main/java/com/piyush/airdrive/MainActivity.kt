package com.piyush.airdrive

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var errorOverlay: View
    private lateinit var errorText: TextView

    private val prefs by lazy { getSharedPreferences("airdrive", Context.MODE_PRIVATE) }
    private val defaultUrl = "http://localhost:8000"

    private fun serverUrl(): String = prefs.getString("server_url", defaultUrl) ?: defaultUrl

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        errorOverlay = findViewById(R.id.errorOverlay)
        errorText = findViewById(R.id.errorText)
        val retryBtn: View = findViewById(R.id.retryBtn)
        val settingsBtn: ImageButton = findViewById(R.id.settingsBtn)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                errorOverlay.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    swipeRefresh.isRefreshing = false
                    webView.visibility = View.GONE
                    errorOverlay.visibility = View.VISIBLE
                    errorText.text = "Can't reach the AirDrive server at\n${serverUrl()}\n\n" +
                        "Make sure it's running in Termux:\nuvicorn main:app --host 0.0.0.0 --port 8000"
                }
            }
        }

        swipeRefresh.setOnRefreshListener { webView.reload() }
        retryBtn.setOnClickListener { webView.loadUrl(serverUrl()) }
        settingsBtn.setOnClickListener { showServerDialog() }

        webView.loadUrl(serverUrl())
    }

    private fun showServerDialog() {
        val input = EditText(this).apply { setText(serverUrl()) }
        AlertDialog.Builder(this)
            .setTitle("Server address")
            .setMessage("Change this if your server runs on a different port or address (e.g. your Tailscale IP for remote access).")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newUrl = input.text.toString().trim().ifEmpty { defaultUrl }
                prefs.edit().putString("server_url", newUrl).apply()
                webView.loadUrl(newUrl)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

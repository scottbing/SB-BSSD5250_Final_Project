package edu.nmhu.bssd5250.sb_maps_demo

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        // get the passed data
        url = intent.getStringExtra("url")
        webView = findViewById<View>(R.id.webview) as WebView
        val webSettings = webView!!.settings
        webSettings.javaScriptEnabled = true // recommended setting
        webView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView!!.settings.blockNetworkLoads = false
        webView!!.loadUrl(url!!)
        webView!!.webViewClient = object : WebViewClient() {
            //
            // Method onReceivedError() will show as deprecated without adding the code between the comment lines
            // This code will handle onReceiveErroor() at various versions of the Adnroid SDK
            // https://stackoverflow.com/questions/32769505/webviewclient-onreceivederror-deprecated-new-version-does-not-detect-all-errors
            // 11/08/2019
            //
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                // Handle the error
                webView!!.loadUrl("file:///android_asset/error.html")
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView,
                req: WebResourceRequest,
                rerr: WebResourceError
            ) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(
                    view,
                    rerr.errorCode,
                    rerr.description.toString(),
                    req.url.toString()
                )
            } //
            // End of Annotated Code
            //
            //
        }
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        } else {
            finish()
        }
    }
}
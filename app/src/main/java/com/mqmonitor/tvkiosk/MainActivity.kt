package com.mqmonitor.tvkiosk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var activeDialog: AlertDialog? = null

    private val prefs by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webView = WebView(this)
        setContentView(webView)

        enableImmersiveMode()
        configureWebView()
        setupBackHandler()

        webView.loadUrl(getSavedUrl())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            showUrlSettingsDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        webView.destroy()
        super.onDestroy()
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, webView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    val targetUrl = getSavedUrl()
                    val html = """
                        <html>
                        <body style="background:#0f172a;color:#f8fafc;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center;">
                            <h2>ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้</h2>
                            <p style="color:#94a3b8;">$targetUrl</p>
                            <p>กำลังลองเชื่อมต่อใหม่อัตโนมัติใน 5 วินาที...<br/>(กดปุ่ม Back บนรีโมทเพื่อแก้ไข IP / URL)</p>
                            <script>setTimeout(function(){ window.location.href = "$targetUrl"; }, 5000);</script>
                        </body>
                        </html>
                    """.trimIndent()
                    view?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            }
        }
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitOrSettingsDialog()
            }
        })
    }

    private fun showExitOrSettingsDialog() {
        if (activeDialog?.isShowing == true) return

        activeDialog = AlertDialog.Builder(this)
            .setTitle("ออกจากโหมด Kiosk?")
            .setMessage("ต้องการปิดแอปพลิเคชันหรือไม่\nURL ปัจจุบัน: ${getSavedUrl()}")
            .setPositiveButton("ปิดแอป") { _, _ ->
                finishAffinity()
            }
            .setNeutralButton("แก้ไข IP / URL") { _, _ ->
                showUrlSettingsDialog()
            }
            .setNegativeButton("ยกเลิก") { dialog, _ ->
                dialog.dismiss()
                enableImmersiveMode()
            }
            .setOnDismissListener {
                enableImmersiveMode()
            }
            .show()
    }

    private fun showUrlSettingsDialog() {
        activeDialog?.dismiss()

        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
        }

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setText(getSavedUrl())
            setSelection(text.length)
            hint = getString(R.string.default_url)
            isSingleLine = true
        }
        container.addView(input)

        activeDialog = AlertDialog.Builder(this)
            .setTitle("ตั้งค่า IP / URL")
            .setMessage("ระบุ URL ของ MQMonitor ที่ต้องการแสดงผล:")
            .setView(container)
            .setPositiveButton("บันทึกและโหลดใหม่") { _, _ ->
                val normalized = normalizeUrl(input.text.toString())
                if (normalized.isNotEmpty()) {
                    saveUrl(normalized)
                    webView.loadUrl(normalized)
                    Toast.makeText(this, "บันทึก URL แล้ว: $normalized", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("คืนค่าเริ่มต้น") { _, _ ->
                val defaultUrl = getString(R.string.default_url)
                saveUrl(defaultUrl)
                webView.loadUrl(defaultUrl)
                Toast.makeText(this, "คืนค่าเริ่มต้นแล้ว", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("ยกเลิก") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                enableImmersiveMode()
            }
            .show()
    }

    private fun getSavedUrl(): String {
        return prefs.getString(KEY_URL, null) ?: getString(R.string.default_url)
    }

    private fun saveUrl(url: String) {
        prefs.edit().putString(KEY_URL, url).apply()
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    companion object {
        private const val PREFS_NAME = "mqmonitor_kiosk_prefs"
        private const val KEY_URL = "kiosk_url"
    }
}

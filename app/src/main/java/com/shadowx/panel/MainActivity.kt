package com.shadowx.panel

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private lateinit var floatingBall: TextView
    private lateinit var panel: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var periodText: TextView
    private lateinit var balanceText: TextView
    private lateinit var resultText: TextView
    private lateinit var actionButton: Button

    private lateinit var tts: TextToSpeech

    private val handler = Handler(Looper.getMainLooper())

    private val allowedNumbers = setOf(
        "8800319125",
        "8800318888"
    )

    private val preferencesName = "shadow_x_memory"
    private val registeredNumbersKey = "registered_numbers"

    private var currentPhone = ""
    private var currentBalance = 0.0
    private var currentPeriod = ""
    private var panelVisible = false
    private var processing = false

    private val websiteUrl =
        "https://www.82winoo.com/#/register?invitationCode=782544845183"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)

        setupWebView()
        setupFloatingBall()
        setupPanel()

        setContentView(root)

        webView.loadUrl(websiteUrl)

        startWebsiteMonitor()

        speak(
            "यह पुरानी ID पर काम नहीं करेगा, कृपया नया ID बनाएँ।"
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        webView = WebView(this)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)

                injectMonitorScript()

                handler.postDelayed({
                    injectMonitorScript()
                }, 1000)
            }
        }

        webView.addJavascriptInterface(
            WebsiteBridge(),
            "ShadowXBridge"
        )

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun setupFloatingBall() {

        floatingBall = TextView(this)

        floatingBall.text = "SHADOW\nX"
        floatingBall.textSize = 11f
        floatingBall.setTextColor(Color.WHITE)
        floatingBall.gravity = Gravity.CENTER
        floatingBall.setTypeface(null, android.graphics.Typeface.BOLD)

        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.rgb(20, 0, 0))
        drawable.setStroke(
            dp(3),
            Color.rgb(255, 0, 0)
        )

        floatingBall.background = drawable
        floatingBall.elevation = dp(12).toFloat()

        val params = FrameLayout.LayoutParams(
            dp(70),
            dp(70)
        )

        params.gravity =
            Gravity.END or Gravity.CENTER_VERTICAL

        params.setMargins(
            0,
            0,
            dp(12),
            0
        )

        root.addView(
            floatingBall,
            params
        )

        enableDragging()

        floatingBall.setOnClickListener {

            if (!panelVisible) {
                showPanel()
            }
        }
    }

    private fun setupPanel() {

        panel = LinearLayout(this)

        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        val background = GradientDrawable()
        background.setColor(Color.rgb(10, 5, 5))
        background.setStroke(
            dp(2),
            Color.rgb(220, 0, 0)
        )
        background.cornerRadius = dp(18).toFloat()

        panel.background = background
        panel.elevation = dp(20).toFloat()

        val params = FrameLayout.LayoutParams(
            dp(300),
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        params.gravity = Gravity.CENTER

        root.addView(panel, params)

        panel.visibility = View.GONE

        val title = TextView(this)

        title.text = "SHADOW X"
        title.textSize = 23f
        title.setTextColor(Color.RED)
        title.gravity = Gravity.CENTER
        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        panel.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        statusText = makeText("ACCESS LOCKED", 16f)
        panel.addView(statusText)

        periodText = makeText("PERIOD: --", 14f)
        panel.addView(periodText)

        balanceText = makeText("BALANCE: ₹0.00", 14f)
        panel.addView(balanceText)

        resultText = makeText("RESULT: --", 20f)
        resultText.setTextColor(Color.WHITE)

        panel.addView(
            resultText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        actionButton = Button(this)
        actionButton.text = "REGISTER"

        panel.addView(
            actionButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        val hideButton = Button(this)

        hideButton.text = "HIDE"

        panel.addView(
            hideButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )
        )

        actionButton.setOnClickListener {

            if (currentPhone.isEmpty()) {
                webView.loadUrl(websiteUrl)
            } else {
                executePrediction()
            }
        }

        hideButton.setOnClickListener {
            hidePanel()
        }
    }

    private fun makeText(
        text: String,
        size: Float
    ): TextView {

        val view = TextView(this)

        view.text = text
        view.textSize = size
        view.setTextColor(Color.WHITE)
        view.gravity = Gravity.CENTER
        view.setPadding(
            0,
            dp(5),
            0,
            dp(5)
        )

        return view
    }

    private fun showPanel() {

        panelVisible = true
        floatingBall.visibility = View.GONE
        panel.visibility = View.VISIBLE

        refreshPanel()
    }

    private fun hidePanel() {

        panelVisible = false
        panel.visibility = View.GONE
        floatingBall.visibility = View.VISIBLE
    }

    private fun refreshPanel() {

        if (currentPhone.isEmpty()) {

            statusText.text = "REGISTER FIRST"
            periodText.text = "ACCESS LOCKED"
            balanceText.text = "LOGIN REQUIRED"
            resultText.text = "RESULT: --"

            actionButton.text = "REGISTER"

            return
        }

        val allowed =
            allowedNumbers.contains(currentPhone) ||
            isRegisteredNumber(currentPhone)

        if (!allowed) {

            statusText.text = "ACCESS DENIED"
            periodText.text = "REGISTER THROUGH SHADOW X"
            balanceText.text = "NUMBER NOT AUTHORIZED"
            resultText.text = "RESULT: --"

            actionButton.text = "REGISTER"

            return
        }

        statusText.text = "SHADOW X ACTIVE"

        periodText.text =
            if (currentPeriod.isEmpty())
                "PERIOD: --"
            else
                "PERIOD: $currentPeriod"

        balanceText.text =
            "BALANCE: ₹%.2f".format(currentBalance)

        if (currentBalance < 100.0) {

            actionButton.text = "DEPOSIT"
            resultText.text = "MINIMUM BALANCE ₹100"

        } else {

            actionButton.text = "EXECUTE"

            if (!processing &&
                !resultText.text.toString().startsWith("AI RESULT")
            ) {
                resultText.text = "READY"
            }
        }
    }

    private fun executePrediction() {

        if (currentBalance < 100.0) {

            webView.loadUrl(
                "javascript:(function(){"
                    + "var a=[...document.querySelectorAll('a,button,div')];"
                    + "var x=a.find(e=>e.innerText&&e.innerText.toLowerCase().includes('deposit'));"
                    + "if(x)x.click();"
                    + "})()"
            )

            return
        }

        if (currentPeriod.isEmpty()) {

            resultText.text =
                "PERIOD NOT FOUND"

            return
        }

        processing = true

        actionButton.text = "WAIT"

        resultText.text =
            "PROCESSING..."

        handler.postDelayed({

            val number =
                calculateNumber(currentPeriod)

            val size =
                if (number <= 4)
                    "SMALL"
                else
                    "BIG"

            resultText.text =
                "AI RESULT $size $number"

            actionButton.text =
                "EXECUTE"

            processing = false

        }, 1800)
    }

    private fun calculateNumber(
        period: String
    ): Int {

        var sum = 0

        for (char in period) {

            if (char.isDigit()) {
                sum += char.digitToInt()
            }
        }

        while (sum >= 10) {

            var next = 0

            sum.toString().forEach {
                next += it.digitToInt()
            }

            sum = next
        }

        return sum
    }

    private fun startWebsiteMonitor() {

        handler.post(object : Runnable {

            override fun run() {

                if (!isFinishing) {

                    injectMonitorScript()

                    handler.postDelayed(
                        this,
                        1500
                    )
                }
            }
        })
    }

    private fun injectMonitorScript() {

        val script = """
            (function() {

                function getText() {
                    return document.body
                        ? document.body.innerText
                        : "";
                }

                var text = getText();

                var numbers =
                    text.match(/\b[6-9][0-9]{9}\b/g);

                if (numbers && numbers.length > 0) {
                    ShadowXBridge.phone(numbers[0]);
                }

                var money =
                    text.match(
                        /(?:₹|Rs\.?|INR)\s*[0-9,]+(?:\.[0-9]+)?/gi
                    );

                if (money && money.length > 0) {

                    var value =
                        money[0]
                        .replace(/[^0-9.]/g, "");

                    ShadowXBridge.balance(value);
                }

                var periods =
                    text.match(/\b[0-9]{10,25}\b/g);

                if (periods && periods.length > 0) {

                    var candidate =
                        periods[periods.length - 1];

                    ShadowXBridge.period(candidate);
                }

                var lower =
                    text.toLowerCase();

                if (
                    lower.includes("access denied") ||
                    lower.includes("please register first")
                ) {
                    ShadowXBridge.denied();
                }

                if (
                    lower.includes("registration successful") ||
                    lower.includes("register successfully") ||
                    lower.includes("account created") ||
                    lower.includes("login successful")
                ) {
                    ShadowXBridge.success();
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(
            script,
            null
        )
    }

    inner class WebsiteBridge {

        @JavascriptInterface
        fun phone(number: String) {

            runOnUiThread {

                val clean =
                    number.filter { it.isDigit() }

                if (clean.length == 10) {

                    currentPhone = clean

                    val allowed =
                        allowedNumbers.contains(clean) ||
                        isRegisteredNumber(clean)

                    if (allowed) {
                        refreshPanel()
                    }
                }
            }
        }

        @JavascriptInterface
        fun balance(value: String) {

            runOnUiThread {

                val clean =
                    value.replace(",", "")

                currentBalance =
                    clean.toDoubleOrNull() ?: currentBalance

                refreshPanel()
            }
        }

        @JavascriptInterface
        fun period(value: String) {

            runOnUiThread {

                if (value.length >= 10) {

                    currentPeriod = value

                    refreshPanel()
                }
            }
        }

        @JavascriptInterface
        fun success() {

            runOnUiThread {

                if (currentPhone.isNotEmpty()) {

                    saveRegisteredNumber(
                        currentPhone
                    )

                    speak(
                        "हैक को एक्टिव करने के लिए मिनिमम 300 का डिपॉजिट करें।"
                    )

                    refreshPanel()
                }
            }
        }

        @JavascriptInterface
        fun denied() {

            runOnUiThread {

                speak(
                    "आपने रजिस्टर बायपास करने की कोशिश की है, कृपया नया अकाउंट बनाएँ।"
                )

                currentPhone = ""

                refreshPanel()
            }
        }
    }

    private fun saveRegisteredNumber(
        number: String
    ) {

        if (number.isEmpty()) return

        val prefs =
            getSharedPreferences(
                preferencesName,
                MODE_PRIVATE
            )

        val existing =
            prefs.getStringSet(
                registeredNumbersKey,
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        existing.add(number)

        prefs.edit()
            .putStringSet(
                registeredNumbersKey,
                existing
            )
            .apply()
    }

    private fun isRegisteredNumber(
        number: String
    ): Boolean {

        val prefs =
            getSharedPreferences(
                preferencesName,
                MODE_PRIVATE
            )

        val saved =
            prefs.getStringSet(
                registeredNumbersKey,
                emptySet()
            )

        return saved?.contains(number) == true
    }

    private fun enableDragging() {

        var downX = 0f
        var downY = 0f
        var startX = 0f
        var startY = 0f

        floatingBall.setOnTouchListener { view, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX = event.rawX
                    downY = event.rawY

                    startX = view.x
                    startY = view.y

                    false
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx =
                        event.rawX - downX

                    val dy =
                        event.rawY - downY

                    view.x =
                        startX + dx

                    view.y =
                        startY + dy

                    true
                }

                MotionEvent.ACTION_UP -> {

                    val dx =
                        event.rawX - downX

                    val dy =
                        event.rawY - downY

                    if (
                        kotlin.math.abs(dx) < dp(10) &&
                        kotlin.math.abs(dy) < dp(10)
                    ) {
                        view.performClick()
                    }

                    true
                }

                else -> false
            }
        }
    }

    private fun speak(text: String) {

        if (::tts.isInitialized) {

            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "shadow_x_voice"
            )
        }
    }

    override fun onInit(status: Int) {

        if (status ==
            TextToSpeech.SUCCESS
        ) {

            tts.language =
                Locale("hi", "IN")
        }
    }

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    override fun onBackPressed() {

        if (panelVisible) {

            hidePanel()
            return
        }

        if (webView.canGoBack()) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        if (::tts.isInitialized) {

            tts.stop()
            tts.shutdown()
        }

        webView.destroy()

        super.onDestroy()
    }
}

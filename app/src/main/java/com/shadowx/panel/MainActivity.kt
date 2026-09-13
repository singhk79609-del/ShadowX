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
import kotlin.math.abs

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
    private var lastAnnouncedSuccessPhone = ""

    private val registerUrl =
        "https://www.82winoo.com/#/register?invitationCode=782544845183"

    private val mainUrl =
        "https://www.82winoo.com/#/main"

    /*
     * USER PROVIDED URL
     *
     * WinGo_30S = 30 second game.
     * If 1 minute is required, change WinGo_30S to WinGo_1M.
     */
    private val gameUrl =
        "https://www.82winoo.com/#/saasLottery/WinGo?gameCode=WinGo_30S&lottery=WinGo"


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

        webView.loadUrl(registerUrl)

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
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)

                injectWebsiteMonitor()

                handler.postDelayed({
                    injectWebsiteMonitor()
                }, 700)
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

        floatingBall.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val drawable = GradientDrawable()

        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.rgb(20, 0, 0))
        drawable.setStroke(
            dp(3),
            Color.RED
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
            showPanel()
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

        background.setColor(
            Color.rgb(10, 5, 5)
        )

        background.setStroke(
            dp(2),
            Color.RED
        )

        background.cornerRadius =
            dp(18).toFloat()

        panel.background = background
        panel.elevation = dp(20).toFloat()

        val params = FrameLayout.LayoutParams(
            dp(300),
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        params.gravity = Gravity.CENTER

        root.addView(
            panel,
            params
        )

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


        statusText =
            makeText("REGISTER FIRST", 16f)

        panel.addView(statusText)


        periodText =
            makeText("PERIOD: --", 14f)

        panel.addView(periodText)


        balanceText =
            makeText("BALANCE: ₹0.00", 14f)

        panel.addView(balanceText)


        resultText =
            makeText("RESULT: --", 20f)

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

            /*
             * IMPORTANT:
             * Never execute prediction for an unauthorized number.
             */

            if (currentPhone.isEmpty()) {

                webView.loadUrl(registerUrl)
                return@setOnClickListener
            }


            if (!isPhoneAuthorized(currentPhone)) {

                webView.loadUrl(registerUrl)

                speak(
                    "आपने रजिस्टर बायपास करने की कोशिश की है, कृपया नया अकाउंट बनाएँ।"
                )

                return@setOnClickListener
            }


            if (currentBalance < 100.0) {

                openDeposit()

                return@setOnClickListener
            }


            executePrediction()
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

        floatingBall.visibility =
            View.GONE

        panel.visibility =
            View.VISIBLE

        refreshPanel()
    }


    private fun hidePanel() {

        panelVisible = false

        panel.visibility =
            View.GONE

        floatingBall.visibility =
            View.VISIBLE
    }


    private fun refreshPanel() {

        if (currentPhone.isEmpty()) {

            statusText.text =
                "REGISTER FIRST"

            periodText.text =
                "ACCESS LOCKED"

            balanceText.text =
                "LOGIN REQUIRED"

            resultText.text =
                "RESULT: --"

            actionButton.text =
                "REGISTER"

            return
        }


        if (!isPhoneAuthorized(currentPhone)) {

            statusText.text =
                "ACCESS DENIED"

            periodText.text =
                "REGISTER THROUGH SHADOW X"

            balanceText.text =
                "NUMBER NOT AUTHORIZED"

            resultText.text =
                "RESULT: --"

            actionButton.text =
                "REGISTER"

            return
        }


        statusText.text =
            "SHADOW X ACTIVE"


        periodText.text =
            if (currentPeriod.isEmpty()) {
                "PERIOD: --"
            } else {
                "PERIOD: $currentPeriod"
            }


        balanceText.text =
            "BALANCE: ₹%.2f".format(
                currentBalance
            )


        if (currentBalance < 100.0) {

            actionButton.text =
                "DEPOSIT"

            resultText.text =
                "MINIMUM BALANCE ₹100"

        } else {

            actionButton.text =
                if (processing)
                    "WAIT"
                else
                    "EXECUTE"

            if (!processing &&
                !resultText.text
                    .toString()
                    .startsWith("AI RESULT")
            ) {

                resultText.text =
                    "READY"
            }
        }
    }


    private fun executePrediction() {

        if (!isPhoneAuthorized(currentPhone)) {
            return
        }


        if (currentBalance < 100.0) {

            openDeposit()

            return
        }


        if (currentPeriod.isEmpty()) {

            resultText.text =
                "PERIOD NOT FOUND"

            /*
             * Go directly to the actual WinGo page
             * so the next monitor cycle can read
             * the current period.
             */

            webView.loadUrl(gameUrl)

            return
        }


        processing = true

        actionButton.text =
            "WAIT"

        resultText.text =
            "PROCESSING..."

        handler.postDelayed({

            if (isFinishing) {
                return@postDelayed
            }

            val number =
                calculateNumber(currentPeriod)


            val size =
                if (number <= 4)
                    "SMALL"
                else
                    "BIG"


            resultText.text =
                "AI RESULT $size $number"

            processing = false

            actionButton.text =
                "EXECUTE"

        }, 1200)
    }


    private fun calculateNumber(
        period: String
    ): Int {

        var sum = 0

        period.forEach { char ->

            if (char.isDigit()) {
                sum += char.digitToInt()
            }
        }


        while (sum >= 10) {

            var next = 0

            sum.toString().forEach { char ->

                next += char.digitToInt()
            }

            sum = next
        }

        return sum
    }


    private fun openDeposit() {

        /*
         * Ask the website to click its own
         * Deposit button.
         */

        val script = """
            (function() {

                var elements =
                    document.querySelectorAll(
                        'button,a,div,span'
                    );

                for (
                    var i = 0;
                    i < elements.length;
                    i++
                ) {

                    var text =
                        (elements[i].innerText || '')
                        .trim()
                        .toLowerCase();

                    if (
                        text === 'deposit' ||
                        text.includes('deposit')
                    ) {

                        elements[i].click();

                        return;
                    }
                }

            })();
        """.trimIndent()

        webView.evaluateJavascript(
            script,
            null
        )
    }


    private fun startWebsiteMonitor() {

        handler.post(
            object : Runnable {

                override fun run() {

                    if (!isFinishing) {

                        injectWebsiteMonitor()

                        handler.postDelayed(
                            this,
                            1000
                        )
                    }
                }
            }
        )
    }


    private fun injectWebsiteMonitor() {

        val script = """
            (function() {

                try {

                    var url =
                        window.location.href || '';

                    var body =
                        document.body
                        ? document.body.innerText
                        : '';


                    /*
                     * PHONE
                     */

                    var phoneRegex =
                        /\b[6-9][0-9]{9}\b/g;

                    var phones =
                        body.match(phoneRegex);

                    if (
                        phones &&
                        phones.length > 0
                    ) {

                        ShadowXBridge.phone(
                            phones[0]
                        );
                    }


                    /*
                     * CURRENT PERIOD
                     *
                     * Only read period on the
                     * actual WinGo page.
                     */

                    if (
                        url.includes(
                            '/saasLottery/WinGo'
                        )
                    ) {

                        var period =
                            findCurrentPeriod(body);

                        if (period) {

                            ShadowXBridge.period(
                                period
                            );
                        }
                    }


                    /*
                     * BALANCE
                     *
                     * Only read balance from
                     * the main page.
                     */

                    if (
                        url.includes('/main')
                    ) {

                        var balance =
                            findBalance(body);

                        if (balance !== null) {

                            ShadowXBridge.balance(
                                balance
                            );
                        }
                    }


                    /*
                     * ACCESS DENIED
                     */

                    var lower =
                        body.toLowerCase();

                    if (
                        lower.includes(
                            'access denied'
                        ) ||
                        lower.includes(
                            'please register first'
                        )
                    ) {

                        ShadowXBridge.denied();
                    }


                    /*
                     * REGISTRATION / LOGIN
                     */

                    if (
                        lower.includes(
                            'registration successful'
                        ) ||
                        lower.includes(
                            'register successfully'
                        ) ||
                        lower.includes(
                            'account created'
                        ) ||
                        lower.includes(
                            'login successful'
                        )
                    ) {

                        ShadowXBridge.success();
                    }


                    /*
                     * Keep checking SPA route
                     * changes even when the page
                     * itself does not reload.
                     */

                    setTimeout(
                        function() {
                            try {
                                ShadowXBridge.route(
                                    window.location.href
                                );
                            } catch(e) {}
                        },
                        50
                    );

                } catch(e) {}


                function findCurrentPeriod(text) {

                    var lines =
                        text
                        .split('\\n')
                        .map(function(x) {
                            return x.trim();
                        })
                        .filter(function(x) {
                            return x.length > 0;
                        });


                    /*
                     * First preference:
                     * line near "current period".
                     */

                    for (
                        var i = 0;
                        i < lines.length;
                        i++
                    ) {

                        var lower =
                            lines[i].toLowerCase();

                        if (
                            lower.includes(
                                'current period'
                            ) ||
                            lower === 'period' ||
                            lower.includes(
                                'current issue'
                            )
                        ) {

                            for (
                                var j = i;
                                j < Math.min(
                                    i + 4,
                                    lines.length
                                );
                                j++
                            ) {

                                var match =
                                    lines[j].match(
                                        /\\b[0-9]{10,25}\\b/
                                    );

                                if (match) {
                                    return match[0];
                                }
                            }
                        }
                    }


                    /*
                     * Second preference:
                     * common long period number.
                     */

                    var all =
                        text.match(
                            /\\b[0-9]{15,25}\\b/g
                        );

                    if (
                        all &&
                        all.length > 0
                    ) {

                        return all[0];
                    }

                    return null;
                }


                function findBalance(text) {

                    var lines =
                        text
                        .split('\\n')
                        .map(function(x) {
                            return x.trim();
                        })
                        .filter(function(x) {
                            return x.length > 0;
                        });


                    for (
                        var i = 0;
                        i < lines.length;
                        i++
                    ) {

                        var lower =
                            lines[i].toLowerCase();


                        if (
                            lower.includes('balance') ||
                            lower.includes('wallet')
                        ) {

                            /*
                             * Check current line.
                             */

                            var match =
                                lines[i].match(
                                    /(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)/i
                                );

                            if (match) {

                                return match[1]
                                    .replace(
                                        /,/g,
                                        ''
                                    );
                            }


                            /*
                             * Check next few lines.
                             */

                            for (
                                var j = i + 1;
                                j < Math.min(
                                    i + 4,
                                    lines.length
                                );
                                j++
                            ) {

                                var nextMatch =
                                    lines[j].match(
                                        /(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)/i
                                    );

                                if (nextMatch) {

                                    return nextMatch[1]
                                        .replace(
                                            /,/g,
                                            ''
                                        );
                                }
                            }
                        }
                    }


                    return null;
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
                    number.filter {
                        it.isDigit()
                    }

                if (clean.length == 10) {

                    currentPhone =
                        clean

                    refreshPanel()
                }
            }
        }


        @JavascriptInterface
        fun balance(value: String) {

            runOnUiThread {

                val clean =
                    value.replace(
                        ",",
                        ""
                    )

                val parsed =
                    clean.toDoubleOrNull()

                if (parsed != null) {

                    currentBalance =
                        parsed

                    refreshPanel()
                }
            }
        }


        @JavascriptInterface
        fun period(value: String) {

            runOnUiThread {

                val clean =
                    value.filter {
                        it.isDigit()
                    }

                if (
                    clean.length >= 10 &&
                    clean.length <= 25
                ) {

                    currentPeriod =
                        clean

                    refreshPanel()
                }
            }
        }


        @JavascriptInterface
        fun route(url: String) {

            runOnUiThread {

                /*
                 * Route changed inside SPA.
                 * Refresh monitor immediately.
                 */

                injectWebsiteMonitor()
            }
        }


        @JavascriptInterface
        fun success() {

            runOnUiThread {

                if (
                    currentPhone.isNotEmpty() &&
                    currentPhone !=
                    lastAnnouncedSuccessPhone
                ) {

                    saveRegisteredNumber(
                        currentPhone
                    )

                    lastAnnouncedSuccessPhone =
                        currentPhone

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

                if (currentPhone.isNotEmpty()) {

                    speak(
                        "आपने रजिस्टर बायपास करने की कोशिश की है, कृपया नया अकाउंट बनाएँ।"
                    )
                }

                currentPhone = ""

                currentBalance = 0.0
                currentPeriod = ""

                refreshPanel()
            }
        }
    }


    private fun isPhoneAuthorized(
        number: String
    ): Boolean {

        return allowedNumbers.contains(
            number
        ) ||
        isRegisteredNumber(number)
    }


    private fun saveRegisteredNumber(
        number: String
    ) {

        if (number.isEmpty()) {
            return
        }


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


        return saved?.contains(
            number
        ) == true
    }


    private fun enableDragging() {

        var downX = 0f
        var downY = 0f

        var startX = 0f
        var startY = 0f


        floatingBall.setOnTouchListener {
            view,
            event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX =
                        event.rawX

                    downY =
                        event.rawY

                    startX =
                        view.x

                    startY =
                        view.y

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
                        abs(dx) < dp(10) &&
                        abs(dy) < dp(10)
                    ) {

                        view.performClick()
                    }

                    true
                }


                else -> false
            }
        }
    }


    private fun speak(
        text: String
    ) {

        if (::tts.isInitialized) {

            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "shadow_x_voice"
            )
        }
    }


    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            tts.language =
                Locale(
                    "hi",
                    "IN"
                )
        }
    }


    private fun dp(
        value: Int
    ): Int {

        return (
            value *
            resources.displayMetrics.density
        ).toInt()
    }


    @Suppress("DEPRECATION")
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

        handler.removeCallbacksAndMessages(
            null
        )


        if (::tts.isInitialized) {

            tts.stop()
            tts.shutdown()
        }


        webView.destroy()

        super.onDestroy()
    }
}

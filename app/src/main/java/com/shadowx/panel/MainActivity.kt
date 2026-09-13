package com.shadowx.panel

import android.annotation.SuppressLint
import android.graphics.Color
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

    private lateinit var floatingBall: ShadowBallView
    private lateinit var panel: LinearLayout

    private lateinit var statusText: TextView
    private lateinit var periodText: TextView
    private lateinit var balanceText: TextView
    private lateinit var resultText: TextView
    private lateinit var actionButton: Button

    private lateinit var tts: TextToSpeech

    private val handler =
        Handler(Looper.getMainLooper())

    /*
     * Special permanently allowed numbers.
     */
    private val allowedNumbers = setOf(
        "8800319125",
        "8800318888"
    )

    /*
     * Local SHADOW X registration memory.
     */
    private val preferencesName =
        "shadow_x_memory"

    private val registeredNumbersKey =
        "registered_numbers"

    private var currentPhone = ""

    private var currentBalance = 0.0

    private var currentPeriod = ""

    private var panelVisible = false

    private var processing = false

    private var lastSuccessAnnouncementPhone = ""

    /*
     * 82 Winoo pages.
     */
    private val registerUrl =
        "https://www.82winoo.com/#/register?invitationCode=782544845183"

    private val mainUrl =
        "https://www.82winoo.com/#/main"

    /*
     * NOTE:
     * WinGo_30S is the 30-second game.
     *
     * If the actual required game is 1 minute,
     * change WinGo_30S to WinGo_1M.
     */
    private val gameUrl =
        "https://www.82winoo.com/#/saasLottery/WinGo?gameCode=WinGo_30S&lottery=WinGo"


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(
            this,
            this
        )

        root = FrameLayout(this)

        root.setBackgroundColor(
            Color.BLACK
        )

        setupWebView()

        setupFloatingBall()

        setupPanel()

        setContentView(root)

        webView.loadUrl(
            registerUrl
        )

        startWebsiteMonitor()

        speak(
            "यह पुरानी ID पर काम नहीं करेगा, कृपया नया ID बनाएँ।"
        )
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        webView = WebView(this)

        webView.settings.javaScriptEnabled =
            true

        webView.settings.domStorageEnabled =
            true

        webView.settings.databaseEnabled =
            true

        webView.settings.allowFileAccess =
            true

        webView.settings.allowContentAccess =
            true

        webView.settings.javaScriptCanOpenWindowsAutomatically =
            true

        webView.settings.loadsImagesAutomatically =
            true

        webView.webViewClient =
            object : WebViewClient() {

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    super.onPageFinished(
                        view,
                        url
                    )

                    injectWebsiteMonitor()

                    handler.postDelayed(
                        {
                            injectWebsiteMonitor()
                        },
                        500
                    )
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


    /*
     * Floating SHADOW X logo ball.
     */
    private fun setupFloatingBall() {

        floatingBall =
            ShadowBallView(this)

        floatingBall.elevation =
            dp(15).toFloat()

        val params =
            FrameLayout.LayoutParams(
                dp(82),
                dp(82)
            )

        params.gravity =
            Gravity.END or
                    Gravity.CENTER_VERTICAL

        params.setMargins(
            0,
            0,
            dp(10),
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


    /*
     * Main SHADOW X panel.
     */
    private fun setupPanel() {

        panel =
            LinearLayout(this)

        panel.orientation =
            LinearLayout.VERTICAL

        panel.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        val background =
            android.graphics.drawable.GradientDrawable()

        background.setColor(
            Color.rgb(
                10,
                5,
                5
            )
        )

        background.setStroke(
            dp(2),
            Color.RED
        )

        background.cornerRadius =
            dp(18).toFloat()

        panel.background =
            background

        panel.elevation =
            dp(20).toFloat()

        val panelParams =
            FrameLayout.LayoutParams(
                dp(310),
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

        panelParams.gravity =
            Gravity.CENTER

        root.addView(
            panel,
            panelParams
        )

        panel.visibility =
            View.GONE


        val title =
            TextView(this)

        title.text =
            "SHADOW X"

        title.textSize =
            23f

        title.setTextColor(
            Color.RED
        )

        title.gravity =
            Gravity.CENTER

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
            makeText(
                "REGISTER FIRST",
                16f
            )

        panel.addView(
            statusText
        )


        periodText =
            makeText(
                "PERIOD: --",
                14f
            )

        panel.addView(
            periodText
        )


        balanceText =
            makeText(
                "BALANCE: ₹0.00",
                14f
            )

        panel.addView(
            balanceText
        )


        resultText =
            makeText(
                "RESULT: --",
                20f
            )

        panel.addView(
            resultText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )


        actionButton =
            Button(this)

        actionButton.text =
            "REGISTER"

        panel.addView(
            actionButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )


        val hideButton =
            Button(this)

        hideButton.text =
            "HIDE"

        panel.addView(
            hideButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )
        )


        actionButton.setOnClickListener {

            handleActionButton()
        }


        hideButton.setOnClickListener {

            hidePanel()
        }
    }


    private fun handleActionButton() {

        /*
         * Not logged in / phone not detected.
         */
        if (currentPhone.isEmpty()) {

            webView.loadUrl(
                registerUrl
            )

            return
        }


        /*
         * Phone exists but SHADOW X has not
         * authorized it.
         */
        if (
            !isPhoneAuthorized(
                currentPhone
            )
        ) {

            webView.loadUrl(
                registerUrl
            )

            speak(
                "आपने रजिस्टर बायपास करने की कोशिश की है, कृपया नया अकाउंट बनाएँ।"
            )

            return
        }


        /*
         * Authorized but balance below
         * prediction threshold.
         */
        if (currentBalance < 100.0) {

            openDeposit()

            return
        }


        executePrediction()
    }


    private fun makeText(
        text: String,
        size: Float
    ): TextView {

        val view =
            TextView(this)

        view.text =
            text

        view.textSize =
            size

        view.setTextColor(
            Color.WHITE
        )

        view.gravity =
            Gravity.CENTER

        view.setPadding(
            0,
            dp(5),
            0,
            dp(5)
        )

        return view
    }


    private fun showPanel() {

        panelVisible =
            true

        floatingBall.visibility =
            View.GONE

        panel.visibility =
            View.VISIBLE

        refreshPanel()
    }


    private fun hidePanel() {

        panelVisible =
            false

        panel.visibility =
            View.GONE

        floatingBall.visibility =
            View.VISIBLE
    }


    private fun refreshPanel() {

        /*
         * No phone detected.
         */
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


        /*
         * Phone detected but unauthorized.
         */
        if (
            !isPhoneAuthorized(
                currentPhone
            )
        ) {

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


        /*
         * Authorized user.
         */
        statusText.text =
            "SHADOW X ACTIVE"


        periodText.text =
            if (
                currentPeriod.isEmpty()
            ) {
                "PERIOD: --"
            } else {
                "PERIOD: $currentPeriod"
            }


        balanceText.text =
            "BALANCE: ₹%.2f".format(
                currentBalance
            )


        /*
         * Prediction gate.
         */
        if (currentBalance < 100.0) {

            actionButton.text =
                "DEPOSIT"

            resultText.text =
                "MINIMUM BALANCE ₹100"

        } else {

            actionButton.text =
                if (processing) {
                    "WAIT"
                } else {
                    "EXECUTE"
                }

            if (
                !processing &&
                !resultText.text
                    .toString()
                    .startsWith(
                        "AI RESULT"
                    )
            ) {

                resultText.text =
                    "READY"
            }
        }
    }


    /*
     * Execute current-period calculation.
     */
    private fun executePrediction() {

        if (
            !isPhoneAuthorized(
                currentPhone
            )
        ) {
            return
        }


        if (currentBalance < 100.0) {

            openDeposit()

            return
        }


        /*
         * If period is missing, open the
         * actual WinGo page.
         */
        if (currentPeriod.isEmpty()) {

            resultText.text =
                "PERIOD NOT FOUND"

            webView.loadUrl(
                gameUrl
            )

            return
        }


        processing =
            true

        actionButton.text =
            "WAIT"

        resultText.text =
            "PROCESSING..."


        /*
         * Fresh calculation after a short
         * processing animation.
         */
        handler.postDelayed({

            if (isFinishing) {
                return@postDelayed
            }


            val periodAtExecution =
                currentPeriod


            val number =
                calculateNumber(
                    periodAtExecution
                )


            val size =
                if (number <= 4) {
                    "SMALL"
                } else {
                    "BIG"
                }


            resultText.text =
                "AI RESULT $size $number"


            processing =
                false

            actionButton.text =
                "EXECUTE"

        }, 1200)
    }


    /*
     * Sum all period digits repeatedly
     * until one digit remains.
     */
    private fun calculateNumber(
        period: String
    ): Int {

        var sum =
            0

        period.forEach { char ->

            if (char.isDigit()) {

                sum +=
                    char.digitToInt()
            }
        }


        while (sum >= 10) {

            var next =
                0

            sum.toString().forEach { char ->

                next +=
                    char.digitToInt()
            }

            sum =
                next
        }


        return sum
    }


    /*
     * Ask the 82 Winoo website to open
     * its own Deposit section.
     */
    private fun openDeposit() {

        val script = """
            (function() {

                var elements =
                    document.querySelectorAll(
                        'button,a,[role="button"],div,span'
                    );

                for (
                    var i = 0;
                    i < elements.length;
                    i++
                ) {

                    var text =
                        (
                            elements[i].innerText || ''
                        )
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


    /*
     * Monitor the SPA every second.
     */
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


    /*
     * Read website information according
     * to the current 82 Winoo route.
     */
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
                     * BALANCE ONLY FROM MAIN PAGE.
                     */
                    if (
                        url.indexOf('/main') !== -1
                    ) {

                        var balance =
                            findBalance(body);

                        if (
                            balance !== null
                        ) {

                            ShadowXBridge.balance(
                                balance
                            );
                        }
                    }


                    /*
                     * PERIOD ONLY FROM WinGo PAGE.
                     */
                    if (
                        url.indexOf(
                            '/saasLottery/WinGo'
                        ) !== -1
                    ) {

                        var period =
                            findCurrentPeriod(
                                body
                            );

                        if (period) {

                            ShadowXBridge.period(
                                period
                            );
                        }
                    }


                    /*
                     * Access denied detection.
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
                     * Registration/login success.
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
                     * Detect SPA route changes.
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


                /*
                 * Find current period.
                 */
                function findCurrentPeriod(text) {

                    var lines =
                        text
                        .split('\\n')
                        .map(function(line) {
                            return line.trim();
                        })
                        .filter(function(line) {
                            return line.length > 0;
                        });


                    /*
                     * Prefer text close to
                     * Current Period / Period.
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
                            lower.includes(
                                'current issue'
                            ) ||
                            lower === 'period'
                        ) {

                            for (
                                var j = i;
                                j < Math.min(
                                    i + 5,
                                    lines.length
                                );
                                j++
                            ) {

                                var match =
                                    lines[j].match(
                                        /\b[0-9]{10,25}\b/
                                    );


                                if (match) {

                                    return match[0];
                                }
                            }
                        }
                    }


                    /*
                     * Fallback:
                     * look for long numeric values.
                     */
                    var candidates =
                        text.match(
                            /\b[0-9]{15,25}\b/g
                        );


                    if (
                        candidates &&
                        candidates.length > 0
                    ) {

                        return candidates[0];
                    }


                    return null;
                }


                /*
                 * Find wallet/balance value.
                 */
                function findBalance(text) {

                    var lines =
                        text
                        .split('\\n')
                        .map(function(line) {
                            return line.trim();
                        })
                        .filter(function(line) {
                            return line.length > 0;
                        });


                    for (
                        var i = 0;
                        i < lines.length;
                        i++
                    ) {

                        var lower =
                            lines[i].toLowerCase();


                        if (
                            lower.includes(
                                'balance'
                            ) ||
                            lower.includes(
                                'wallet'
                            )
                        ) {

                            var sameLine =
                                lines[i].match(
                                    /(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)/i
                                );


                            if (sameLine) {

                                return sameLine[1]
                                    .replace(
                                        /,/g,
                                        ''
                                    );
                            }


                            for (
                                var j = i + 1;
                                j < Math.min(
                                    i + 4,
                                    lines.length
                                );
                                j++
                            ) {

                                var next =
                                    lines[j].match(
                                        /(?:₹|rs\\.?|inr)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)/i
                                    );


                                if (next) {

                                    return next[1]
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
        fun phone(
            number: String
        ) {

            runOnUiThread {

                val clean =
                    number.filter {
                        it.isDigit()
                    }


                if (
                    clean.length == 10
                ) {

                    currentPhone =
                        clean

                    refreshPanel()
                }
            }
        }


        @JavascriptInterface
        fun balance(
            value: String
        ) {

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
        fun period(
            value: String
        ) {

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
        fun route(
            url: String
        ) {

            runOnUiThread {

                /*
                 * SPA route changed.
                 * Read the new page immediately.
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
                    lastSuccessAnnouncementPhone
                ) {

                    /*
                     * Save the number permanently
                     * in local SHADOW X memory.
                     */
                    saveRegisteredNumber(
                        currentPhone
                    )


                    lastSuccessAnnouncementPhone =
                        currentPhone


                    speak(
                        "हैक को एक्टिव करने के लिए मिनिमम 300 का डिपॉजिट करें।"
                    )


                    refreshPanel()


                    /*
                     * Move to main page so balance
                     * can be read.
                     */
                    handler.postDelayed({

                        if (!isFinishing) {

                            webView.loadUrl(
                                mainUrl
                            )
                        }

                    }, 700)
                }
            }
        }


        @JavascriptInterface
        fun denied() {

            runOnUiThread {

                if (
                    currentPhone.isNotEmpty()
                ) {

                    speak(
                        "आपने रजिस्टर बायपास करने की कोशिश की है, कृपया नया अकाउंट बनाएँ।"
                    )
                }


                currentPhone =
                    ""

                currentBalance =
                    0.0

                currentPeriod =
                    ""

                refreshPanel()
            }
        }
    }


    /*
     * Check SHADOW X authorization.
     */
    private fun isPhoneAuthorized(
        number: String
    ): Boolean {

        return allowedNumbers.contains(
            number
        ) ||
        isRegisteredNumber(
            number
        )
    }


    /*
     * Save registered phone locally.
     */
    private fun saveRegisteredNumber(
        number: String
    ) {

        if (
            number.isEmpty()
        ) {
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
            )
                ?.toMutableSet()
                ?: mutableSetOf()


        existing.add(
            number
        )


        prefs.edit()
            .putStringSet(
                registeredNumbersKey,
                existing
            )
            .apply()
    }


    /*
     * Check locally saved numbers.
     */
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


    /*
     * Drag the floating logo around the screen.
     */
    private fun enableDragging() {

        var downX =
            0f

        var downY =
            0f

        var startX =
            0f

        var startY =
            0f


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

                    true
                }


                MotionEvent.ACTION_MOVE -> {

                    val dx =
                        event.rawX -
                                downX

                    val dy =
                        event.rawY -
                                downY


                    view.x =
                        startX + dx

                    view.y =
                        startY + dy

                    true
                }


                MotionEvent.ACTION_UP -> {

                    val dx =
                        event.rawX -
                                downX

                    val dy =
                        event.rawY -
                                downY


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

        if (
            ::tts.isInitialized
        ) {

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
                    resources
                        .displayMetrics
                        .density
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


        if (
            ::tts.isInitialized
        ) {

            tts.stop()

            tts.shutdown()
        }


        webView.destroy()

        super.onDestroy()
    }
}

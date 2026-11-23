
package in.anuj.upisoundbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var history: TextView
    private lateinit var total: TextView
    private var testEngine: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("upi_box", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 42, 36, 24)
        }
        layout.addView(TextView(this).apply {
            text = "UPI PAYMENT ALERT"
            textSize = 25f
            setPadding(0,0,0,8)
        })
        layout.addView(TextView(this).apply {
            text = "UPI ऐप्स के पेमेंट नोटिफिकेशन को पढ़कर हिंदी में अलर्ट देगा।"
            textSize = 15f
            setPadding(0, 4, 0, 18)
        })
        status = TextView(this).apply { textSize = 15f }
        layout.addView(status)

        layout.addView(Button(this).apply {
            text = "Notification Access सेट करें"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        })

        val voiceSwitch = Switch(this).apply {
            text = "हिंदी आवाज़ चालू रखें"
            isChecked = prefs.getBoolean("voice_on", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("voice_on", checked).apply()
            }
        }
        layout.addView(voiceSwitch)

        val soundSwitch = Switch(this).apply {
            text = "अलर्ट साउंड चालू रखें"
            isChecked = prefs.getBoolean("sound_on", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("sound_on", checked).apply()
            }
        }
        layout.addView(soundSwitch)

        val nightSwitch = Switch(this).apply {
            text = "Night Silent Mode (रात 10 से सुबह 7 बजे)"
            isChecked = prefs.getBoolean("night_silent", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("night_silent", checked).apply()
            }
        }
        layout.addView(nightSwitch)

        layout.addView(Button(this).apply {
            text = "टेस्ट हिंदी आवाज़ सुनें"
            setOnClickListener {
                testEngine?.shutdown()
                testEngine = TextToSpeech(this@MainActivity) { result ->
                    if (result == TextToSpeech.SUCCESS) {
                        testEngine?.language = Locale("hi", "IN")
                        testEngine?.speak(
                            "यह यूपीआई पेमेंट अलर्ट का परीक्षण है।",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "test_voice"
                        )
                    }
                }
            }
        })

        total = TextView(this).apply {
            textSize = 20f
            setPadding(0, 20, 0, 12)
        }
        layout.addView(total)

        layout.addView(TextView(this).apply {
            text = "पेमेंट हिस्ट्री (नोटिफिकेशन आधारित)"
            textSize = 17f
            setPadding(0, 12, 0, 8)
        })

        history = TextView(this).apply { textSize = 14f }
        layout.addView(history)

        layout.addView(Button(this).apply {
            text = "पेमेंट हिस्ट्री साफ करें"
            setOnClickListener {
                prefs.edit().remove("history")
                    .remove("total_received").apply()
                refreshHistory()
            }
        })

        layout.addView(TextView(this).apply {
            text = "ध्यान दें: यह बैंक से पेमेंट सत्यापित नहीं करता। नोटिफिकेशन गलत या नकली हो सकते हैं। भुगतान की पुष्टि अपने UPI/बैंक ऐप में करें। Notification Access देने पर ऐप नोटिफिकेशन पढ़ सकता है। यह ऐप UPI PIN या OTP नहीं माँगता।"
            textSize = 12f
            setPadding(0, 24, 0, 0)
        })

        setContentView(ScrollView(this).apply { addView(layout) })
    }

    override fun onResume() {
        super.onResume()
        val enabled = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )?.contains(packageName) == true

        status.text = if (enabled)
            "स्थिति: Notification Access चालू है"
        else
            "स्थिति: Notification Access बंद है"

        refreshHistory()
    }

    private fun refreshHistory() {
        val prefs = getSharedPreferences("upi_box", MODE_PRIVATE)
        val amount = prefs.getFloat("total_received", 0f)
        total.text = "कुल नोटिफिकेशन से प्राप्त: ₹" +
            String.format(Locale.US, "%.2f", amount)

        val items = prefs.getString("history", "").orEmpty()
        history.text = if (items.isBlank())
            "अभी कोई अलर्ट नहीं।"
        else
            items
    }

    override fun onDestroy() {
        testEngine?.shutdown()
        super.onDestroy()
    }
}

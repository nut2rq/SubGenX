package com.subgenx.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private var selectedAudio: Uri? = null
    private lateinit var fileLabel: TextView
    private lateinit var language: Spinner
    private lateinit var status: TextView
    private val pickAudio = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFFF7F8FC.toInt()
        window.navigationBarColor = 0xFFF7F8FC.toInt()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(18))
            setBackgroundColor(0xFFF7F8FC.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(20))
        }

        content.addView(TextView(this).apply {
            text = "SubGenX"
            textSize = 30f
            setTextColor(0xFF172033.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "Audio → subtitles, made simple"
            textSize = 15f
            setTextColor(0xFF687386.toInt())
            setPadding(0, dp(4), 0, dp(26))
        })

        content.addView(TextView(this).apply {
            text = "Create subtitles"
            textSize = 21f
            setTextColor(0xFF172033.toInt())
            setTypeface(null, Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "Choose an audio file, then select the language you want."
            textSize = 14f
            setTextColor(0xFF687386.toInt())
            setPadding(0, dp(6), 0, dp(18))
        })

        val choose = Button(this).apply {
            text = "Choose audio file"
            isAllCaps = false
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                }
                startActivityForResult(intent, pickAudio)
            }
        }
        content.addView(choose, matchWrap())

        fileLabel = TextView(this).apply {
            text = "No file selected"
            textSize = 14f
            setTextColor(0xFF687386.toInt())
            setPadding(0, dp(12), 0, dp(24))
        }
        content.addView(fileLabel)

        content.addView(TextView(this).apply {
            text = "Translate into"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF172033.toInt())
            setPadding(0, 0, 0, dp(8))
        })
        language = Spinner(this)
        val languages = listOf("Thai", "English", "Chinese (Simplified)", "Japanese", "Korean", "Spanish", "French", "German")
        language.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        content.addView(language, matchWrap())

        val start = Button(this).apply {
            text = "Start transcription & translation"
            isAllCaps = false
            setOnClickListener {
                status.text = if (selectedAudio == null) {
                    "Choose an audio file first."
                } else {
                    "Audio selected. Transcription and translation engine will be connected in the next development step."
                }
            }
        }
        val startParams = matchWrap().apply { topMargin = dp(20) }
        content.addView(start, startParams)

        status = TextView(this).apply {
            text = "Ready when you are."
            textSize = 14f
            setTextColor(0xFF687386.toInt())
            setPadding(0, dp(16), 0, dp(18))
        }
        content.addView(status)

        content.addView(TextView(this).apply {
            text = "Simple by design\nAutomatic source-language detection and AI model selection are planned. Your original transcript will remain separate from its translation."
            textSize = 13f
            setTextColor(0xFF687386.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(0xFFECEFF6.toInt())
        })
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickAudio && resultCode == RESULT_OK) {
            selectedAudio = data?.data
            selectedAudio?.let { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fileLabel.text = queryDisplayName(uri) ?: uri.lastPathSegment ?: "Audio file selected"
                status.text = "File selected. Ready for the next step."
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        } catch (_: Exception) { null }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
}

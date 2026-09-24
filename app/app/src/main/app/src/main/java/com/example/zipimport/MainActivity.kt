package com.example.zipimport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import java.util.zip.ZipInputStream

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        val btn = Button(this).apply { text = "Zip tanlash" }
        status = TextView(this).apply { text = "Zip fayl tanlang" }
        layout.addView(btn)
        layout.addView(status)
        setContentView(layout)

        btn.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            startActivityForResult(i, 1)
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        val uri = data?.data
        if (req == 1 && res == RESULT_OK && uri != null) {
            Thread { unzip(uri) }.start()
        }
    }

    private fun unzip(uri: Uri) {
        val outDir = getExternalFilesDir(null)!!
        var count = 0
        try {
            ZipInputStream(contentResolver.openInputStream(uri)).use { zip ->
                var e = zip.nextEntry
                while (e != null) {
                    val f = File(outDir, e.name)
                    if (!f.canonicalPath.startsWith(outDir.canonicalPath)) {
                        throw Exception("Xavfli fayl yo'li")
                    }
                    if (e.isDirectory) {
                        f.mkdirs()
                    } else {
                        f.parentFile?.mkdirs()
                        f.outputStream().use { zip.copyTo(it) }
                    }
                    count++
                    if (count % 20 == 0) {
                        runOnUiThread { status.text = "Ochildi: $count fayl" }
                    }
                    e = zip.nextEntry
                }
            }
            runOnUiThread { status.text = "Tayyor! $count fayl ochildi" }
        } catch (ex: Exception) {
            runOnUiThread { status.text = "Xato: ${ex.message}" }
        }
    }
}

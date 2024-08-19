package com.divyansh.videodownloader

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.divyansh.videodownloader.databinding.ActivityMainBinding
import com.divyansh.videodownloader.utils.showCookieMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvDownloadMsg.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/pratap-divyansh/"))
            startActivity(intent)
        }

        binding.btnDownload.setOnClickListener {
            if(binding.etLink.text.isBlank()){
                binding.tlUrlInput.error="enter a url before proceeding."
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    downloadYouTubeVideo(binding.etLink.text.toString().trim())
                }
                catch (e:Exception){
                    runOnUiThread {
                        handleViewOnFail()
                    }
                }
            }
        }

        binding.etLink.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tlUrlInput.error=null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun downloadYouTubeVideo(url: String) {
        try {
            runOnUiThread {
                binding.lvLoader.visibility = View.VISIBLE
                binding.btnDownload.visibility=View.GONE
            }
            val python = Python.getInstance()
            val pythonModule = python.getModule("youtube_downloader")
            val outputDir = this.filesDir.absolutePath
            val result: PyObject = pythonModule.callAttr("download_video", url, outputDir)
            val message = result.toString()

            if (message.startsWith("DownloadError")) {
                runOnUiThread {
                    handleViewOnFail()
                }
            } else {
                runOnUiThread {
                    binding.btnDownload.visibility=View.VISIBLE
                    binding.lvLoader.visibility = View.GONE
                    binding.etLink.text.clear()
                }
                val currentTime = System.currentTimeMillis()
                saveVideoToGallery(applicationContext, message, "${currentTime.seconds}.mp4")
            }
        } catch (e: PyException) {
            runOnUiThread {
                handleViewOnFail()
            }
        }
    }

    private fun saveVideoToGallery(context: Context, inputPath: String, fileName: String) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    File(inputPath).inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
            }
            runOnUiThread {
                showCookieMessage(this@MainActivity,"Video downloaded & saved  in gallery!")
            }
        }
        catch (e:Exception){
            runOnUiThread {
                handleViewOnFail()
            }
        }
    }

    private fun handleViewOnFail(){
        binding.tlUrlInput.error = "failed downloading!"
        binding.etLink.text.clear()
        binding.btnDownload.visibility=View.VISIBLE
        binding.lvLoader.visibility = View.GONE
    }
}
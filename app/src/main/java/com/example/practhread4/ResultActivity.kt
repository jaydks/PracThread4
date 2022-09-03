package com.example.practhread4

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.practhread4.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mp = MediaPlayer.create(this, R.raw.music_basic)
        mp?.start()

        val score = intent.getIntExtra("score", 0).toString()
        binding.score.text = "${score}원"

        binding.restart.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mp?.pause()
    }

    override fun onResume() {
        super.onResume()
        mp?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.stop()
    }

}
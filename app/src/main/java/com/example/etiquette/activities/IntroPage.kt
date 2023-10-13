package com.example.etiquette.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.etiquette.R
import com.example.etiquette.databinding.IntroPageBinding
import com.example.etiquette.utilities.PreferenceManager

class IntroPage : AppCompatActivity() {
    private lateinit var binding: IntroPageBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var topAnim: Animation
    private lateinit var textAnim: Animation
    private lateinit var logo: ImageView
    private lateinit var welText: TextView
    private lateinit var welMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IntroPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        logo = findViewById(R.id.logo)
        logo.startAnimation(topAnim)

        textAnim = AnimationUtils.loadAnimation(this, R.anim.text_animation)
        welText = findViewById(R.id.WelText) // Correct the variable name
        welText.startAnimation(textAnim)

        textAnim = AnimationUtils.loadAnimation(this, R.anim.text_animation)
        welMsg = findViewById(R.id.WelMsg) // Correct the variable name
        welMsg.startAnimation(textAnim)

        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.getStarted.setOnClickListener {
            startActivity(Intent(applicationContext, SignInActivity::class.java))
        }
    }
}

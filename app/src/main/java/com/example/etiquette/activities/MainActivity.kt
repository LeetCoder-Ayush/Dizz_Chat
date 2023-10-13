package com.example.etiquette.activities


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.etiquette.adapters.RecentConversationsAdapter
import com.example.etiquette.databinding.ActivityMainBinding
import com.example.etiquette.models.ChatMessage
import com.example.etiquette.utilities.Constants
import com.example.etiquette.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        init()
        loadUserDetails()
        getToken()
        setListeners()
    }

    private fun init() {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun loadUserDetails() {
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val imageString = preferenceManager.getString(Constants.KEY_IMAGE)
        if (!imageString.isNullOrEmpty()) {
            val bytes = Base64.decode(imageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageProfile.setImageBitmap(bitmap)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                updateToken(token)
            }
            .addOnFailureListener {
                showToast("Failed to retrieve token")
            }
    }

    private fun updateToken(token: String) {
        val userId = preferenceManager.getString(Constants.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)

            val updates = hashMapOf<String, Any>()
            updates[Constants.KEY_FCM_TOKEN] = token

            documentReference.update(updates)
                .addOnFailureListener {
                    showToast("Unable to update token")
                }
        }
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener { signOut() }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun signOut() {
        showToast("Signing out...")
        val userId = preferenceManager.getString(Constants.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)

            val updates = hashMapOf<String, Any>()
            updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()

            documentReference.update(updates)
                .addOnSuccessListener {
                    preferenceManager.clear()
                    startActivity(Intent(applicationContext, SignInActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    showToast("Unable to sign out")
                }
        }
    }
}
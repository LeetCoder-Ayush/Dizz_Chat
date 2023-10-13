package com.example.etiquette.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.etiquette.adapters.UsersAdapter
import com.example.etiquette.databinding.ActivityUsersBinding
import com.example.etiquette.listeners.UserListener
import com.example.etiquette.models.User
import com.example.etiquette.utilities.Constants
import com.example.etiquette.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

@Suppress("DEPRECATION")
class UsersActivity : AppCompatActivity(), UserListener {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                if (task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (queryDocumentSnapshot in task.result!!) {
                        val userId = queryDocumentSnapshot.id
                        if (currentUserId == userId) {
                            continue
                        }
                        val user = queryDocumentSnapshot.toObject(User::class.java)
                        user.id = queryDocumentSnapshot.id
                        users.add(user)
                    }
                    if (users.isNotEmpty()) {
                        val usersAdapter = UsersAdapter(users, this)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun showErrorMessage() {
        binding.textErrorMessage.text = "No users available"
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}
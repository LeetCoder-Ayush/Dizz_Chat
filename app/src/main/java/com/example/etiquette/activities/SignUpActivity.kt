package com.example.etiquette.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import com.example.etiquette.databinding.ActivitySignUpBinding
import com.example.etiquette.utilities.Constants
import com.example.etiquette.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileNotFoundException
import java.io.InputStream

private lateinit var preferenceManager: PreferenceManager

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener { onBackPressed() }
        binding.buttonSignup.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
    private fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String, Any> = HashMap()

        user[Constants.KEY_NAME] = binding.inputName.text.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage!!

        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)

                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }

            .addOnFailureListener { exception ->
                loading(false)
                exception.message?.let { showToast(it) }
            }
    }
    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val imageUri: Uri? = data.data
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textAddImage.visibility = View.GONE
                    encodedImage = bitmap?.let { encodeImage(it) }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidSignUpDetails(): Boolean {
        if (encodedImage == null) {
            showToast("Select profile image")
            return false
        } else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            return false
        } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (!binding.inputPassword.text.toString().equals(binding.inputConfirmPassword.text.toString())) {
            showToast("Password & Confirm password must be same")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignup.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignup.visibility = View.VISIBLE
        }
    }
}
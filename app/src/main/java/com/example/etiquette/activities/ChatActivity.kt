package com.example.etiquette.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.example.etiquette.adapters.ChatAdapter
import com.example.etiquette.databinding.ActivityChatBinding
import com.example.etiquette.models.ChatMessage
import com.example.etiquette.models.User
import com.example.etiquette.utilities.Constants
import com.example.etiquette.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = mutableListOf()
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitmapFromEncodedString(receiverUser.image),
            preferenceManager.getString(Constants.KEY_USER_ID) ?: ""
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = mutableMapOf<String, Any>(
            Constants.KEY_SENDER_ID to preferenceManager.getString(Constants.KEY_USER_ID)!!,
            Constants.KEY_RECEIVER_ID to receiverUser.id,
            Constants.KEY_MESSAGE to binding.inputMessage.text.toString(),
            Constants.KEY_TIMESTAMP to Date()
        )
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .add(message)
            .addOnSuccessListener { documentReference ->
                val conversationId = documentReference.id
                if (this.conversationId != null) {
                    updateConversation(binding.inputMessage.text.toString())
                } else {
                    val conversation = mutableMapOf<String, Any>(
                        Constants.KEY_SENDER_ID to preferenceManager.getString(Constants.KEY_USER_ID)!!,
                        Constants.KEY_SENDER_NAME to preferenceManager.getString(Constants.KEY_NAME)!!,
                        Constants.KEY_SENDER_IMAGE to preferenceManager.getString(Constants.KEY_IMAGE)!!,
                        Constants.KEY_RECEIVER_ID to receiverUser.id,
                        Constants.KEY_RECEIVER_NAME to receiverUser.name,
                        Constants.KEY_RECEIVER_IMAGE to receiverUser.image,
                        Constants.KEY_LAST_MESSAGE to binding.inputMessage.text.toString(),
                        Constants.KEY_TIMESTAMP to Date()
                    )
                    addConversation(conversation)
                }
                binding.inputMessage.text = null
            }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessages() {
        val eventListener = EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }

            if (value != null) {
                val count = chatMessages.size
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage().apply {
                            senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                            receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            message = documentChange.document.getString(Constants.KEY_MESSAGE)
                            dateTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP))
                            dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        }
                        chatMessages.add(chatMessage)
                    }
                }
                chatMessages.sortBy { it.dateObject }

                if (count == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(count, chatMessages.size - count)
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                }

                binding.chatRecyclerView.visibility = View.VISIBLE
            }

            binding.progressBar.visibility = View.GONE
            if (conversationId == null){
                checkForConversation()
            }
        }

        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap? {
        val bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as? User ?: User()
        binding.textName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date?): String {
        val pattern = "MMMM dd, yyyy hh:mm a"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(date!!)
    }

    private fun addConversation(conversion: MutableMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { documentReference ->
                conversationId = documentReference.id
            }
    }

    private fun updateConversation(message: String) {
        val documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
            conversationId.toString()
        )
        val updates = hashMapOf<String, Any>(
            Constants.KEY_LAST_MESSAGE to message,
            Constants.KEY_TIMESTAMP to Date()
        )
        documentReference.update(updates)
    }


    private fun checkForConversation() {
        if (chatMessages.size != 0) {
            checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                receiverUser.id
            )
            checkForConversionRemotely(
                receiverUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(OnCompleteListener)
    }

    private val OnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
            val documentSnapshot = task.result!!.documents[0]
            conversationId = documentSnapshot.id
        }
    }
}

package com.example.etiquette.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.etiquette.databinding.ItemContainerReceivedMessageBinding
import com.example.etiquette.databinding.ItemContainerSentMessageBinding
import com.example.etiquette.models.ChatMessage


class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val receiverProfileImage: Bitmap?,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT_MESSAGE = 1
    private val VIEW_TYPE_RECEIVED_MESSAGE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT_MESSAGE -> {
                val binding = ItemContainerSentMessageBinding.inflate(inflater, parent, false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED_MESSAGE -> {
                val binding = ItemContainerReceivedMessageBinding.inflate(inflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        when (holder) {
            is SentMessageViewHolder -> {
                holder.setData(chatMessage)
            }
            is ReceivedMessageViewHolder -> {
                holder.setData(chatMessage, receiverProfileImage)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = chatMessages[position]
        return if (chatMessage.senderId == senderId) {
            VIEW_TYPE_SENT_MESSAGE
        } else {
            VIEW_TYPE_RECEIVED_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap?) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }
}
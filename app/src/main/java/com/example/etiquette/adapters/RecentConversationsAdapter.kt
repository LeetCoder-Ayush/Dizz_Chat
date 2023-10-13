package com.example.etiquette.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.etiquette.databinding.ItemContainerRecentConversionBinding
import com.example.etiquette.models.ChatMessage

class RecentConversationsAdapter(private val chatMessages: List<ChatMessage>) :
    RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerRecentConversionBinding.inflate(inflater, parent, false)
        return ConversionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class ConversionViewHolder(private val binding: ItemContainerRecentConversionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            val bitmap = getConversionImage(chatMessage.conversionImage)
            binding.imageProfile.setImageBitmap(bitmap)
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
        }

        private fun getConversionImage(encodedImage: String?): Bitmap {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}
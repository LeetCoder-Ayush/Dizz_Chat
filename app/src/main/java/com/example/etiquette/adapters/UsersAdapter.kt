package com.example.etiquette.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.etiquette.databinding.ItemContainerUserBinding
import com.example.etiquette.listeners.UserListener

class UsersAdapter(private val users: ArrayList<com.example.etiquette.models.User>, private val userListener: UserListener) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.setUserData(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(private val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setUserData(user: com.example.etiquette.models.User) {
            binding.textName.text = user.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener {
                userListener.onUserClicked(user)
            }
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
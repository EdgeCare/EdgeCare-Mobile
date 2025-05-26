package com.example.edgecare.adapters

import android.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.edgecare.R
import com.example.edgecare.databinding.ChatItemBinding
import com.example.edgecare.models.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        with(holder.binding) {
            chatMessageTextView.text = message.message

            if (message.isSentByUser) {
                root.gravity = Gravity.END
                //  messageContainer.setBackgroundResource(R.drawable.message_background_user)
                infoIcon.visibility = View.GONE
            } else {
                root.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.message_background_other)
                if (message.isLocalChat){
                    modelTypeTextView.visibility = View.VISIBLE
                    infoIcon.visibility = View.VISIBLE
                    infoIcon.setOnClickListener {
                        AlertDialog.Builder(infoIcon.context)
                            .setTitle("Reports referred for this response:")
                            .setMessage(message.additionalInfo)
                            .setPositiveButton("OK", null)
                            .show()
                        //Toast.makeText(holder.itemView.context, message.additionalInfo, Toast.LENGTH_SHORT).show()

                    }
                }

            }
        }
    }

    override fun getItemCount(): Int = messages.size
}

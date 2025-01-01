package com.example.edgecare.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edgecare.databinding.ItemChatHistoryBinding
import com.example.edgecare.models.Chat

class ChatHistoryAdapter(
    private val chats: List<Chat>,
    private val onChatClicked: (Chat) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding, onChatClicked)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size

    class ChatViewHolder(
        private val binding: ItemChatHistoryBinding,
        private val onChatClicked: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            binding.chatNameTextView.text = chat.chatName
            binding.root.setOnClickListener {
                onChatClicked(chat)
            }
        }
    }
}

package com.example.edgecare.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.edgecare.R
import com.example.edgecare.databinding.ItemChatHistoryBinding
import com.example.edgecare.models.Chat

class ChatHistoryAdapter(
    private val initialChats: List<Chat>,
    private val onChatClicked: (Chat) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder>() {

    private var selectedChatId:Long = 999L
    private val chats = initialChats.toMutableList()

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

    @SuppressLint("NotifyDataSetChanged")
    fun updateChatList(newChats: List<Chat>,chatId:Long) {
        selectedChatId = chatId
        chats.clear()
        chats.addAll(newChats)
        notifyDataSetChanged()
    }

    fun chatSelected(flag:Boolean) {
        selectedChatId = if(!flag){
            999L
        } else{
            chats.first().id
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(
        private val binding: ItemChatHistoryBinding,
        private val onChatClicked: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(chat: Chat) {
            binding.chatNameTextView.text = chat.chatName
            val context = binding.chatNameTextView.context
            val backgroundRes = if (chat.id == selectedChatId) {
                R.drawable.side_nav_bar_selected_button_background
            } else {
                R.drawable.side_nav_bar_button_background
            }
            binding.chatNameTextView.background = ContextCompat.getDrawable(context, backgroundRes)

            binding.root.setOnClickListener {
                onChatClicked(chat)
                selectedChatId = chat.id
                notifyDataSetChanged()
            }
        }
    }
}

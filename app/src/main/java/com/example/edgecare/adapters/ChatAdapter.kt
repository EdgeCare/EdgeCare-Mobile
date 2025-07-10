package com.example.edgecare.adapters


import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
            chatMessageTextView.setBoldMarkdown(message.message)

//            chatMessageTextView.text = message.message

            if (message.isSentByUser) {
                root.gravity = Gravity.END
                messageContainer.setBackgroundResource(R.drawable.message_background_user)
                infoIcon.visibility = View.GONE

            } else {
                root.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.message_background_other)
                if (message.isLocalChat && message.isCompletedResponse){
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

    fun TextView.setBoldMarkdown(text: String) {
        val builder = SpannableStringBuilder()
        var currentIndex = 0

        val regex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = regex.findAll(text)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last

            // Append text before the match
            if (currentIndex < start) {
                builder.append(text.substring(currentIndex, start))
            }

            val boldText = match.groupValues[1]
            val spanStart = builder.length
            builder.append(boldText)
            builder.setSpan(StyleSpan(Typeface.BOLD), spanStart, spanStart + boldText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            currentIndex = end + 1
        }

        // Append remaining text
        if (currentIndex < text.length) {
            builder.append(text.substring(currentIndex))
        }

        this.text = builder
    }

//    fun TextView.setMarkdownText(markdown: String) {
//        val builder = SpannableStringBuilder(markdown)
//
//        // Bold: **text**
//        Regex("\\*\\*(.*?)\\*\\*").findAll(markdown).forEach {
//            val start = it.range.first
//            val end = it.range.last + 1
//            builder.setSpan(
//                StyleSpan(Typeface.BOLD),
//                start,
//                end,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
//
//        // Italic: _text_
//        Regex("_(.*?)_").findAll(builder).forEach {
//            val start = it.range.first
//            val end = it.range.last + 1
//            builder.setSpan(
//                StyleSpan(Typeface.ITALIC),
//                start,
//                end,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
//
//        // Underline: __text__
//        Regex("__(.*?)__").findAll(builder).forEach {
//            val start = it.range.first
//            val end = it.range.last + 1
//            builder.setSpan(
//                UnderlineSpan(),
//                start,
//                end,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
//
//        // Remove the markdown symbols (**/__/_)
//        val cleaned = builder.replace(Regex("(\\*\\*|__|_)"), "")
//
//        this.text = cleaned
//    }

    override fun getItemCount(): Int = messages.size
}

package com.chesnovitskiy.chatapp.ui

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chesnovitskiy.chatapp.R
import com.chesnovitskiy.chatapp.data.Message
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_my_message.*

class MessagesAdapter(private val context: Context) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {
    private var messages: List<Message> = mutableListOf()

    fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view = if (viewType == TYPE_MY_MESSAGE) {
            inflater.inflate(R.layout.item_my_message, parent, false)
        } else {
            inflater.inflate(R.layout.item_other_message, parent, false)
        }

        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val author = message.author

        return if (!author.isNullOrEmpty() && author == PreferenceManager.getDefaultSharedPreferences(
                context
            ).getString("author", "Anonim")
        ) {
            TYPE_MY_MESSAGE
        } else {
            TYPE_OTHER_MESSAGE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(messages[position])

    inner class ViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        override val containerView: View?
            get() = itemView

        fun bind(message: Message) {
            tv_author.text = message.author

            message.message?.let {
                tv_message.text = it
                iv_image.visibility = View.GONE
            }

            message.imageUrl?.let {
                Picasso.get().load(it).into(iv_image)
                iv_image.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val TYPE_MY_MESSAGE = 1
        private const val TYPE_OTHER_MESSAGE = 2
    }
}
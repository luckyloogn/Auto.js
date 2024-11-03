package org.autojs.autojs.ui.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.autojs.autojs.R
import org.autojs.autojs.databinding.ItemKeyStoreBinding

class KeyStoreAdaptor(
    private val keyStoreAdapterCallback: KeyStoreAdapterCallback
) : ListAdapter<KeyStore, KeyStoreAdaptor.KeyStoreViewHolder>(KeyStoreDiffCallback()) {

    class KeyStoreDiffCallback : DiffUtil.ItemCallback<KeyStore>() {
        override fun areItemsTheSame(oldItem: KeyStore, newItem: KeyStore): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        override fun areContentsTheSame(oldItem: KeyStore, newItem: KeyStore): Boolean {
            return oldItem.filename == newItem.filename &&
                    oldItem.password == newItem.password &&
                    oldItem.alias == newItem.alias &&
                    oldItem.aliasPassword == newItem.aliasPassword &&
                    oldItem.verified == newItem.verified
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyStoreViewHolder {
        val binding = ItemKeyStoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KeyStoreViewHolder(binding).apply {
            binding.delete.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    keyStoreAdapterCallback.onDeleteButtonClicked(getItem(adapterPosition))
                }
            }
            binding.verify.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    keyStoreAdapterCallback.onVerifyButtonClicked(getItem(adapterPosition))
                }
            }
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    keyStoreAdapterCallback.onVerifyButtonClicked(getItem(adapterPosition))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: KeyStoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KeyStoreViewHolder(private val binding: ItemKeyStoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KeyStore) {
            binding.apply {
                filename.text = itemView.context.getString(
                    R.string.formatter_str_colon_space_str,
                    itemView.context.getString(R.string.text_new_key_store_filename),
                    item.filename
                )
                alias.text = itemView.context.getString(
                    R.string.formatter_str_colon_space_str,
                    itemView.context.getString(R.string.text_new_key_store_alias),
                    item.alias
                )
                verify.setImageResource(
                    if (item.verified) R.drawable.ic_key_store_verified else R.drawable.ic_key_store_unverified
                )
            }
        }
    }

    interface KeyStoreAdapterCallback {
        fun onDeleteButtonClicked(keyStore: KeyStore)
        fun onVerifyButtonClicked(keyStore: KeyStore)
    }
}

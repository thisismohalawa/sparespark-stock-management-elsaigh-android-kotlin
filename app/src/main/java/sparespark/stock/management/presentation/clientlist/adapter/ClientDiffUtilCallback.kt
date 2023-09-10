package sparespark.stock.management.presentation.clientlist.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.client.Client

class ClientDiffUtilCallback : DiffUtil.ItemCallback<Client>() {
    override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }

    override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }
}

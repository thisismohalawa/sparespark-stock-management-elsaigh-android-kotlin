package sparespark.stock.management.presentation.clientlist.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import sparespark.stock.management.R
import sparespark.stock.management.core.actionDial
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.actionOpenWhatsApp
import sparespark.stock.management.core.view.setTitleColor
import sparespark.stock.management.data.model.client.Client
import sparespark.stock.management.databinding.ItemClientBinding
import sparespark.stock.management.presentation.clientlist.ClientListEvent

class ClientAdapter(
    val event: MutableLiveData<ClientListEvent> = MutableLiveData()
) : ListAdapter<Client, ViewHolder>(ClientDiffUtilCallback()) {
    inner class ClientViewHolder(var binding: ItemClientBinding) : ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemClientBinding =
            ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val client: Client = getItem(position)
        (holder as ClientViewHolder).binding.apply {
            clientName.text = client.name
            clientCity.text = client.cityName

            imgOptions.setOnClickListener {
                val context = it.context
                val popupMenu = PopupMenu(
                    context, imgOptions
                )
                popupMenu.apply {
                    inflate(R.menu.client_option_menu)
                    menu.findItem(R.id.delete_menu).setTitleColor(Color.RED)
                    setOnMenuItemClickListener { item ->
                        when (item?.itemId) {
                            R.id.call_menu -> context.actionDial(client.phoneNum)
                            R.id.msg_menu -> context.actionOpenWhatsApp(client.phoneNum)
                            R.id.delete_menu -> context.actionDisplayConfirmationDialog(
                                R.string.delete_client,
                            ) {
                                event.value =
                                    ClientListEvent.DeleteClient(creationDate = client.creationDate)
                            }
                        }
                        false
                    }
                    show()
                }
            }
        }
        holder.itemView.setOnClickListener {
            event.value = ClientListEvent.OnClientItemClicked(client = client)
        }
    }
}

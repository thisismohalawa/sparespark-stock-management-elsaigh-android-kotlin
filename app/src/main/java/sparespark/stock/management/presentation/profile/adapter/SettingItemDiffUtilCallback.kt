package sparespark.stock.management.presentation.profile.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.other.SettingItem

class SettingItemDiffUtilCallback : DiffUtil.ItemCallback<SettingItem>() {
    override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
        return oldItem.id == newItem.id
    }
}

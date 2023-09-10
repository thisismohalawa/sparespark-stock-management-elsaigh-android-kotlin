package sparespark.stock.management.presentation.profile.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import sparespark.stock.management.core.COLOR_GREEN
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.SETTINGS_CLEAR_CACHE
import sparespark.stock.management.core.SETTINGS_CLEAR_DB
import sparespark.stock.management.core.SETTINGS_LOGOUT
import sparespark.stock.management.core.SETTINGS_TEAM
import sparespark.stock.management.core.SETTING_DELETE_COMPLETED_ITEMS
import sparespark.stock.management.core.SETTING_TEMP
import sparespark.stock.management.data.model.other.SettingItem
import sparespark.stock.management.databinding.ItemSettingsBinding
import sparespark.stock.management.presentation.main.StockActivityEvent

class SettingsAdapter(
    val event: MutableLiveData<StockActivityEvent> = MutableLiveData()
) : ListAdapter<SettingItem, ViewHolder>(SettingItemDiffUtilCallback()) {

    inner class SettingsViewHolder(var binding: ItemSettingsBinding) : ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSettingsBinding =
            ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val settingItem: SettingItem = getItem(position)
        (holder as SettingsViewHolder).binding.apply {
            val itemContext = settingTitle.context
            settingTitle.text = itemContext.getString(settingItem.title)
            settingSubtitle.text = itemContext.getString(settingItem.subTitle)

            if (settingItem.id == SETTINGS_LOGOUT ||
                settingItem.id == SETTINGS_CLEAR_DB ||
                settingItem.id == SETTING_DELETE_COMPLETED_ITEMS
            ) {
                settingTitle.setTextColor(Color.parseColor(COLOR_RED))
                settingSubtitle.setTextColor(Color.parseColor(COLOR_RED))
            } else if (settingItem.id == SETTING_TEMP) {
                settingTitle.setTextColor(Color.parseColor(COLOR_GREEN))
                settingSubtitle.setTextColor(Color.parseColor(COLOR_GREEN))
            }
        }
        holder.itemView.setOnClickListener {
            when (settingItem.id) {
                SETTINGS_LOGOUT -> event.value = StockActivityEvent.Logout
                SETTINGS_CLEAR_CACHE -> event.value = StockActivityEvent.ClearCache
                SETTINGS_TEAM -> event.value = StockActivityEvent.OnTeamSettingClick
                SETTING_TEMP -> event.value = StockActivityEvent.OnTempDBSettingClick
                SETTING_DELETE_COMPLETED_ITEMS -> event.value =
                    StockActivityEvent.OnBackupsActionSettingClick
            }
        }
    }
}

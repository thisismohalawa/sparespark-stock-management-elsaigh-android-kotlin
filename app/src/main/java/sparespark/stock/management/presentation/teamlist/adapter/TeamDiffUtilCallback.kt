package sparespark.stock.management.presentation.teamlist.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.team.MainTeamItem

class TeamDiffUtilCallback : DiffUtil.ItemCallback<MainTeamItem>() {
    override fun areItemsTheSame(oldItem: MainTeamItem, newItem: MainTeamItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MainTeamItem, newItem: MainTeamItem): Boolean {
        return oldItem.id == newItem.id
    }
}

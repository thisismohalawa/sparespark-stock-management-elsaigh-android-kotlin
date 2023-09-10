package sparespark.stock.management.presentation.teamlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.core.view.displayAsLinedBoldStyle
import sparespark.stock.management.core.view.onViewedClickedUpdateExpanding
import sparespark.stock.management.core.view.setupListItemDecoration
import sparespark.stock.management.core.view.updateImageDependOnExpandingLayout
import sparespark.stock.management.data.model.team.MainTeamItem
import sparespark.stock.management.databinding.ItemMainTeamBinding
import sparespark.stock.management.presentation.teamlist.TeamListViewEvent

class MainTeamAdapter(
    val event: MutableLiveData<TeamListViewEvent> = MutableLiveData()
) : ListAdapter<MainTeamItem, RecyclerView.ViewHolder>(TeamDiffUtilCallback()) {
    inner class MainTeamViewHolder(var binding: ItemMainTeamBinding) :
        RecyclerView.ViewHolder(
            binding.root
        )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemMainTeamBinding = ItemMainTeamBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainTeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, itemPosition: Int) {
        val item: MainTeamItem = getItem(itemPosition)
        (holder as MainTeamViewHolder).binding.apply {
            teamTitle.displayAsLinedBoldStyle(teamTitle.context.getString(item.title))
            imgArrow.updateImageDependOnExpandingLayout(expandingLayout)
            viewedLayout.onViewedClickedUpdateExpanding(
                contentLayout = contentLayout,
                expandingLayout = expandingLayout,
                followedAction = {
                    imgArrow.updateImageDependOnExpandingLayout(expandingLayout)
                }
            )
            if (item.team.isNotEmpty()) {
                val teamAdapter = SubTeamAdapter(event = event)
                subRecList.adapter = teamAdapter
                subRecList.setupListItemDecoration(subRecList.context)
                subRecList.isNestedScrollingEnabled = false
                teamAdapter.submitList(item.team)
            }
        }
    }
}

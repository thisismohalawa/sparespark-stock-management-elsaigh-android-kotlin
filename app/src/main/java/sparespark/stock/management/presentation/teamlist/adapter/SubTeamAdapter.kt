package sparespark.stock.management.presentation.teamlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.R
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.data.model.login.User
import sparespark.stock.management.databinding.ItemTeamMemberBinding
import sparespark.stock.management.presentation.teamlist.TeamListViewEvent

class SubTeamAdapter(
    val event: MutableLiveData<TeamListViewEvent> = MutableLiveData()
) : ListAdapter<User, RecyclerView.ViewHolder>(UserDiffUtilCallback()) {
    inner class TeamMemberHolder(var binding: ItemTeamMemberBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemTeamMemberBinding =
            ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamMemberHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val teamMember: User = getItem(position)
        (holder as TeamMemberHolder).binding.apply {
            val itemContext = userName.context
            userName.text = teamMember.name
            userEmail.text = teamMember.email
            activeSwitch.isChecked = teamMember.activated
            activeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView?.isPressed == true)
                    itemContext.actionDisplayConfirmationDialog(
                        R.string.update_user,
                    ) {
                        event.value = TeamListViewEvent.UpdateUserActiveStatus(
                            uid = teamMember.uid,
                            isActive = isChecked
                        )
                    }
            }
            holder.itemView.setOnClickListener {
                event.value = TeamListViewEvent.OnTeamMemberClick(teamMember)
            }
        }
    }
}

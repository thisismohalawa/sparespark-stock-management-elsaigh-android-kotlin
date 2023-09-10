package sparespark.stock.management.presentation.teamlist.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.login.User

class UserDiffUtilCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }
}

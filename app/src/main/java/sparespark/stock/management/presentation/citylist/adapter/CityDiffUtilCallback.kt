package sparespark.stock.management.presentation.citylist.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.city.City

class CityDiffUtilCallback : DiffUtil.ItemCallback<City>() {
    override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }

    override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }
}

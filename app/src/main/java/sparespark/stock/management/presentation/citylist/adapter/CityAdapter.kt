package sparespark.stock.management.presentation.citylist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.R
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.databinding.ItemCityBinding
import sparespark.stock.management.presentation.citylist.CityListEvent

class CityAdapter(
    val event: MutableLiveData<CityListEvent> = MutableLiveData()
) : ListAdapter<City, RecyclerView.ViewHolder>(CityDiffUtilCallback()) {
    inner class CityViewHolder(var binding: ItemCityBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemCityBinding =
            ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val city: City = getItem(position)
        (holder as CityViewHolder).binding.apply {
            cityName.text = city.name
            imgDelete.setOnClickListener {
                it.context.actionDisplayConfirmationDialog(
                    R.string.delete_city
                ) {
                    event.value = CityListEvent.DeleteCity(creationDate = city.creationDate)
                }
            }
        }
        holder.itemView.setOnClickListener {
            event.value = CityListEvent.OnCityItemClicked(city = city)
        }
    }
}

package sparespark.stock.management.presentation.stocklist.adapter

import androidx.recyclerview.widget.DiffUtil
import sparespark.stock.management.data.model.stock.Stock

class StockDiffUtilCallback : DiffUtil.ItemCallback<Stock>() {
    override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }

    override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
        return oldItem.creationDate == newItem.creationDate
    }
}

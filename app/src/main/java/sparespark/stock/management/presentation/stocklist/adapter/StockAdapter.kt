package sparespark.stock.management.presentation.stocklist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sparespark.stock.management.R
import sparespark.stock.management.core.COLOR_BLUE
import sparespark.stock.management.core.COLOR_RED
import sparespark.stock.management.core.actionDisplayConfirmationDialog
import sparespark.stock.management.core.actionShareText
import sparespark.stock.management.core.getActiveTypeStringValue
import sparespark.stock.management.core.getOperationTypeStringValue
import sparespark.stock.management.core.getStockDetailsSharedTitle
import sparespark.stock.management.core.view.markAsCompleted
import sparespark.stock.management.core.view.onViewedClickedUpdateExpanding
import sparespark.stock.management.core.view.removeMarkFlag
import sparespark.stock.management.core.view.setTitleColor
import sparespark.stock.management.core.view.toStringFullNumberFormat
import sparespark.stock.management.core.view.visible
import sparespark.stock.management.data.model.stock.Stock
import sparespark.stock.management.databinding.ItemStockBinding
import sparespark.stock.management.presentation.stocklist.StockListEvent

@SuppressLint("SetTextI18n")
class StockAdapter(
    val event: MutableLiveData<StockListEvent> = MutableLiveData(),
    private var isTempItem: Boolean = false,
) : ListAdapter<Stock, RecyclerView.ViewHolder>(StockDiffUtilCallback()) {

    companion object {
        private val selectedStockIds = arrayListOf<String>()
        private fun isItemSelected(id: String): Boolean = selectedStockIds.contains(id)
        fun clearSelectedStockIds() = selectedStockIds.clear()
    }

    inner class StockViewHolder(var binding: ItemStockBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemStockBinding =
            ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stock: Stock = getItem(position)
        holder.itemView.setOnClickListener {
            event.value = StockListEvent.OnStockItemClicked(stock)
        }
        holder.itemView.setOnLongClickListener {
            val popupMenu = PopupMenu(it.context, holder.itemView.findViewById(R.id.txt_total))
            popupMenu.apply {
                inflate(R.menu.stock_option_menu)
                menu.findItem(R.id.delete_menu).setTitleColor(Color.parseColor(COLOR_RED))
                if (isTempItem) menu.findItem(R.id.select_menu).isVisible = false

                setOnMenuItemClickListener { item ->
                    when (item?.itemId) {
                        R.id.delete_menu -> it.context.actionDisplayConfirmationDialog(titleResId = R.string.delete_item,
                            action = {
                                if (selectedStockIds.size == 0) event.value =
                                    StockListEvent.DeleteStock(
                                        itemId = stock.creationDate,
                                        selectedItems = null,
                                        isTemp = isTempItem
                                    )
                                else event.value = StockListEvent.DeleteStock(
                                    itemId = null,
                                    selectedItems = selectedStockIds,
                                    isTemp = false // temp data item selection is disabled.
                                )
                            })

                        R.id.select_menu -> holder.itemView.findViewById<RelativeLayout>(
                            R.id.content_layout
                        ).selectGroupOfItemItem(stock.creationDate)

                        R.id.share_menu -> it.context.actionShareText(
                            getStockDetailsSharedTitle(
                                title = "Shared",
                                date = stock.creationDateCustom,
                                type = stock.operationType.getOperationTypeStringValue(),
                                client = stock.client,
                                city = stock.city,
                                gramPrice = stock.assetGramPrice,
                                quantity = stock.assetQuantity
                            )
                        )
                    }
                    false
                }
                show()
            }
            true
        }
        (holder as StockViewHolder).binding.apply {
            try {
                val itemContext = txtClient.context

                txtSerial.setTextDateFormat(
                    date = stock.creationDateCustom
                )
                txtClient.setClientDetails(
                    client = stock.client, city = stock.city
                )
                infoImg.setStockImgInfo(
                    isBuy = stock.operationType,
                    expandingLayout = expandableLayout,
                    viewedLayout = viewedLayout
                )

                txtTotal.setTextStockTotal(
                    gramPrice = stock.assetGramPrice,
                    quantity = stock.assetQuantity,
                    isBuy = stock.operationType,
                    isActive = stock.active,
                    context = itemContext
                )
                txtAssetInfo.setTextAssetDetails(
                    assetPrice = stock.assetGramPrice,
                    quantity = stock.assetQuantity,
                    context = itemContext
                )
                txtCreatedInfo.setTextCreatorDetails(
                    creator = stock.createdBy, context = itemContext
                )

                txtUpdateInfo.setTextUpdateDetails(
                    updateBy = stock.lastUpdateBy,
                    updateAt = stock.lastUpdateDate,
                    context = itemContext
                )
                txtDetails.setTextNoteDetails(
                    details = stock.details,
                    expandingLayout = expandableLayout
                )

                if (!isTempItem) {
                    activeCheckbox.setStockActivateBox(stock.active)
                    activeCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (buttonView?.isPressed == true) itemContext.actionDisplayConfirmationDialog(
                            titleResId = R.string.update_item,
                            msgString = (!isChecked).getActiveTypeStringValue()
                        ) {
                            if (selectedStockIds.size == 0) event.value =
                                StockListEvent.UpdateStockStatus(
                                    itemId = stock.creationDate,
                                    selectedItems = null,
                                    isActive = !isChecked
                                )
                            else event.value = StockListEvent.UpdateStockStatus(
                                itemId = null,
                                selectedItems = selectedStockIds,
                                isActive = !isChecked
                            )
                        }
                    }
                } else expandableLayout.visible(true)


            } catch (ex: Exception) {
                txtTotal.text = "Error!"
                txtClient.text = "Exception: ${ex.message.toString()}"
            }
        }
    }

    private fun TextView.setTextDateFormat(date: String) {
        this.text = date
    }

    private fun TextView.setClientDetails(
        client: String, city: String
    ) {
        this.text = "$client - $city"
    }

    private fun ImageView.setStockImgInfo(
        isBuy: Boolean, expandingLayout: ViewGroup, viewedLayout: ViewGroup
    ) {
        if (isBuy) this.setImageResource(R.drawable.ic_buy)
        else this.setImageResource(R.drawable.ic_sell)

        this.onViewedClickedUpdateExpanding(contentLayout = viewedLayout,
            expandingLayout = expandingLayout,
            followedAction = {})
    }

    private fun TextView.setTextStockTotal(
        gramPrice: Double, quantity: Double, isBuy: Boolean, isActive: Boolean, context: Context
    ) {
        this.text =
            (gramPrice * quantity).toStringFullNumberFormat() + " (${quantity.toStringFullNumberFormat()} " + "${
                context.getString(R.string.g)
            })"
        if (isBuy) this.setTextColor(Color.parseColor(COLOR_BLUE))
        else this.setTextColor(Color.parseColor(COLOR_RED))

        if (!isActive) this.markAsCompleted()
        else this.removeMarkFlag()
    }


    private fun CheckBox.setStockActivateBox(isActive: Boolean) {
        this.isChecked = !isActive
    }

    private fun TextView.setTextAssetDetails(
        assetPrice: Double, quantity: Double, context: Context
    ) {
        this.text =
            context.getString(R.string.asset_price) + ": " + assetPrice.toStringFullNumberFormat() +
                    "\n" + context.getString(
                R.string.asset_quantity
            ) + ": " + quantity.toStringFullNumberFormat()
    }

    private fun TextView.setTextCreatorDetails(
        creator: String, context: Context
    ) {
        this.text = context.getString(R.string.created_by) + ": " + creator
    }

    private fun TextView.setTextUpdateDetails(
        updateBy: String, updateAt: String, context: Context
    ) {
        if (updateBy.isNotBlank()) this.text = context.getString(
            R.string.update_by
        ) + ": " + updateBy + "\t$updateAt"
        else this.visible(false)
    }

    private fun TextView.setTextNoteDetails(
        details: String,
        expandingLayout: ViewGroup
    ) {
        if (details.isNotBlank()) {
            this.text = details
            if (!isTempItem) expandingLayout.visible(true)
        } else this.visible(false)
    }

    private fun RelativeLayout.selectGroupOfItemItem(creationDate: String) {
        if (isItemSelected(creationDate)) {
            this.setBackgroundResource(R.color.white)
            selectedStockIds.remove(creationDate)
        } else {
            selectedStockIds.add(creationDate)
            this.setBackgroundResource(R.drawable.item_rounded_layout_green)
        }
    }
}

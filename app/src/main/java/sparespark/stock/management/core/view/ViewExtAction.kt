package sparespark.stock.management.core.view

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.google.android.material.textfield.TextInputLayout
import sparespark.stock.management.core.FILTERED_ALL
import sparespark.stock.management.core.handlerPostDelayed
import sparespark.stock.management.data.model.city.City
import sparespark.stock.management.data.model.client.Client


internal fun Spinner.setUpCitySpinners(
    context: Context,
    cityList: List<City>,
    allAsFirstItem: Boolean = false,
    action: ((String) -> Unit)? = null
) {
    val cityNames: MutableList<String> = mutableListOf()
    if (allAsFirstItem) cityNames.add(0, FILTERED_ALL)
    for (i in cityList.indices) cityNames.add(cityList[i].name)

    val aa: ArrayAdapter<String> = ArrayAdapter(context, R.layout.simple_spinner_item, cityNames)
    aa.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    this.adapter = aa
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View?, position: Int, id: Long
        ) {

            if (position != 0) action?.invoke(cityNames[position])

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

internal fun Spinner.setUpClientSpinners(
    context: Context, clientList: List<Client>, action: ((String) -> Unit)? = null
) {
    val clientNames: MutableList<String> = mutableListOf()
    clientNames.add(0, FILTERED_ALL)

    for (i in clientList.indices) {
        clientNames.add(clientList[i].name)
    }

    val aa: ArrayAdapter<String> = ArrayAdapter(context, R.layout.simple_spinner_item, clientNames)
    aa.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

    this.adapter = aa
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View?, position: Int, id: Long
        ) {
            if (position != 0) action?.invoke(clientNames[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

internal fun SearchView.setUpSearchViewListenerByQuery(
    action: ((String) -> Unit)? = null
) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let { query ->
                if (query.length > 1) handlerPostDelayed(2000) {
                    action?.invoke(query)
                }
            }
            return true
        }
    })
}

internal fun AutoCompleteTextView.setUpClientAutoComplete(
    context: Context, clientList: List<Client>, action: ((String) -> Unit)? = null
) = try {
    val clientNames: MutableList<String> = mutableListOf()

    for (i in clientList.indices) {
        clientNames.add(clientList[i].name)
    }
    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
        context, R.layout.simple_dropdown_item_1line, clientNames
    )
    this.setAdapter(arrayAdapter)
    this.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        action?.invoke(
            adapter.getItem(position).toString()
        )
    }
} catch (ex: Exception) {
    Toast.makeText(context, "Error ${ex.message}", Toast.LENGTH_SHORT).show()
}

internal fun TextInputLayout.setIconAction(
    icon: Int, action: () -> Unit
) {
    setEndIconActivated(true)
    isEndIconVisible = true
    setEndIconDrawable(icon)
    setEndIconOnClickListener {
        action()
    }
}

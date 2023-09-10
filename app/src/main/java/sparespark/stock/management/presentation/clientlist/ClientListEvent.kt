package sparespark.stock.management.presentation.clientlist

import sparespark.stock.management.data.model.client.Client

sealed class ClientListEvent {
    object OnDestroy : ClientListEvent()
    object OnClientViewStart : ClientListEvent()
    object GetClientList : ClientListEvent()
    object GetCityList : ClientListEvent()
    data class OnClientItemClicked(val client: Client) : ClientListEvent()
    data class UpdateClient(val clientName: String, val phoneNum: String) : ClientListEvent()
    data class DeleteClient(val creationDate: String) : ClientListEvent()
    object UpdateBottomSheetToHideState : ClientListEvent()
}

package sparespark.stock.management.data.repository

import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.client.Client

interface ClientRepository {
    suspend fun getClientList(): DataResult<Exception, List<Client>>
    suspend fun updateClient(client: Client): DataResult<Exception, Unit>
    suspend fun deleteClient(creationDate: String): DataResult<Exception, Unit>
    suspend fun getCityByClientName(client: String): DataResult<Exception, String?>
}

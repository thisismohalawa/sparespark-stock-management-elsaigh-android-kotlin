package sparespark.stock.management.data.repository

import sparespark.stock.management.core.result.DataResult
import sparespark.stock.management.data.model.city.City

interface CityRepository {
    suspend fun getCityList(): DataResult<Exception, List<City>>
    suspend fun updateCity(city: City): DataResult<Exception, Unit>
    suspend fun deleteCity(creationDate: String): DataResult<Exception, Unit>

}

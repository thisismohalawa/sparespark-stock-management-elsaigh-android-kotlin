package sparespark.stock.management.core.result

sealed class DataResult<out E, out V> {

    data class Value<out V>(val value: V) : DataResult<Nothing, V>()
    data class Error<out E>(val error: E) : DataResult<E, Nothing>()

    companion object Factory {
        inline fun <V> build(function: () -> V): DataResult<Exception, V> =
            try {
                Value(function.invoke())
            } catch (e: java.lang.Exception) {
                Error(e)
            }
    }
}

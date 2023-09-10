package sparespark.stock.management.core

import java.io.IOException

const val NO_INTERNET_CONNECTION = "No Internet Connection."
const val DEACTIVATED = "Deactivated."
const val NOT_PERMITTED = "Not Permitted."

class NoConnectivityException : IOException(NO_INTERNET_CONNECTION)
class DeactivatedException : Exception(DEACTIVATED)
class NotPermittedException : Exception(NOT_PERMITTED)

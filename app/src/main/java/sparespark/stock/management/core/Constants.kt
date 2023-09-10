package sparespark.stock.management.core

// INFO
internal const val TAG = "TAG::DEBUG"
internal const val FILTERED_ALL = "~"
internal const val IS_BUY = true
internal const val IS_PENDING = true


// MAX
internal const val MAX_GRAM_DIG = 8
internal const val MAX_TOTAL_DIG = 12
internal const val MAX_CLIENT_NAME = 50
internal const val MAX_INPUT_LEN = 200

// ROLE
internal const val ACTIVE = true
internal const val OWNER_ROLE_ID = 1
internal const val ADMIN_ROLE_ID = 2
internal const val PM_ROLE_ID = 3
internal const val EMPLOYEE_ROLE_ID = 4

// COLOR
internal const val COLOR_GRAY = "#ffffff"
internal const val COLOR_BLUE = "#086FC1"
internal const val COLOR_RED = "#c46e6e"
internal const val COLOR_GREEN = "#5eab3d"


// REMOTE
internal const val SIGN_IN_REQUEST_CODE = 1227
internal const val DATABASE_URL =
    "https://elsaigh-stock-mangement-default-rtdb.europe-west1.firebasedatabase.app/"
internal const val DATABASE_REF_NAME = "stock_database"
internal const val TEAM_REF_NAME = "team_list"
internal const val CITY_REF_NAME = "city_list"
internal const val PAY_REF_NAME = "pay_list"
internal const val CLIENT_REF_NAME = "client_list"
internal const val ACTIVATED_CHILD_NAME = "activated"
internal const val ROLE_ID_CHILD_NAME = "roleId"
internal const val PAY_ACTIVE_REF_NAME = "active"
internal const val UPDATED_REF_NAME = "lastUpdateBy"
internal const val UPDATED_DATE_REF_NAME = "lastUpdateDate"

// SETTINGS
internal const val SETTINGS_TEAM = 1
internal const val SETTINGS_CITY = 2
internal const val SETTINGS_CLIENT = 3
internal const val SETTING_TEMP = 6
internal const val SETTING_DELETE_COMPLETED_ITEMS = 8
internal const val SETTINGS_CLEAR_CACHE = 4
internal const val SETTINGS_LOGOUT = 5
internal const val SETTINGS_CLEAR_DB = 7

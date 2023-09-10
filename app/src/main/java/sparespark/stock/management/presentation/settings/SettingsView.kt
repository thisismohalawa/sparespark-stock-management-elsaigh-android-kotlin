package sparespark.stock.management.presentation.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import sparespark.stock.management.R

class SettingsView : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}

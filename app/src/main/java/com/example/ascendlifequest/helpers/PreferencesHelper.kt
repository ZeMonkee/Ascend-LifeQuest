import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    private const val PREFS_NAME = "user_preferences"
    const val KEY_SPORT = "sport_preference"
    const val KEY_CUISINE = "cuisine_preference"
    const val KEY_JEUX_VIDEO = "jeux_video_preference"
    const val KEY_LECTURE = "lecture_preference"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun savePreference(context: Context, key: String, value: Int) {
        val prefs = getSharedPreferences(context)
        with(prefs.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getPreference(context: Context, key: String, defaultValue: Int): Int {
        val prefs = getSharedPreferences(context)
        return prefs.getInt(key, defaultValue)
    }
}

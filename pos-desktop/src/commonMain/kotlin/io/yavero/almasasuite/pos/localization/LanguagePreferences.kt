package io.yavero.almasasuite.pos.localization


interface LanguagePreferences {
    fun saveLanguage(language: Language)
    fun getLanguage(): Language
}


class InMemoryLanguagePreferences : LanguagePreferences {
    private var savedLanguage: Language = Language.ENGLISH

    override fun saveLanguage(language: Language) {
        savedLanguage = language
    }

    override fun getLanguage(): Language {
        return savedLanguage
    }
}


class PersistentLocalizationManager(
    private val preferences: LanguagePreferences = InMemoryLanguagePreferences()
) {
    private val localizationManager = LocalizationManager()

    val currentLanguage = localizationManager.currentLanguage

    init {

        val savedLanguage = preferences.getLanguage()
        localizationManager.setLanguage(savedLanguage)
    }

    fun getString(key: String): String {
        return localizationManager.getString(key)
    }

    fun setLanguage(language: Language) {
        localizationManager.setLanguage(language)
        preferences.saveLanguage(language)
    }

    fun toggleLanguage() {
        localizationManager.toggleLanguage()
        preferences.saveLanguage(currentLanguage.value)
    }
}
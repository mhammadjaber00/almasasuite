package io.yavero.almasasuite.pos.localization


enum class Language(
    val code: String,
    val displayName: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", false),
    ARABIC("ar", "العربية", true);

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
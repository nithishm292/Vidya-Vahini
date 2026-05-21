package com.example.vidyavahini.utils

object TranslatorManager {
    private val dictionary = mapOf(
        "mysore" to "ಮೈಸೂರು",
        "yelwala" to "ಯಲವಾಲ",
        "railway station" to "ರೈಲ್ವೆ ನಿಲ್ದಾಣ",
        "bus stand" to "ಬಸ್ ನಿಲ್ದಾಣ",
        "kuvempunagar" to "ಕುವೆಂಪುನಗರ",
        "vontikoppal" to "ವಂಟಿಕೊಪ್ಪಲ್",
        "vijayanagar" to "ವಿಜಯನಗರ",
        "hebbal" to "ಹೆಬ್ಬಾಳ್",
        "bogadi" to "ಬೋಗಾದಿ",
        "jp nagar" to "ಜೆಪಿ ನಗರ",
        "srirampura" to "ಶ್ರೀರಾಮಪುರ"
    )

    fun translateEnglishToKannada(text: String): String {
        val lowerText = text.lowercase().trim()
        return dictionary[lowerText] ?: text
    }
}

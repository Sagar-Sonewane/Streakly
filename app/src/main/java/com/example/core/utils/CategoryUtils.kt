package com.example.core.utils

object CategoryUtils {
    fun getCategoryLabel(emoji: String, language: String): String {
        return when (emoji) {
            "🏋️", "🏃", "🚴", "🏊", "🚶", "⚽", "🏀", "🥗", "💧", "💊", "🍏", "🍌", "🚿", "🧼", "🧘" -> when (language) {
                "hi" -> "स्वास्थ्य और फिटनेस"
                "mr" -> "आरोग्य आणि तंदुरुस्ती"
                else -> "Health & Fitness"
            }
            "📚", "🧠", "🏫", "✏️", "✍️", "🧩", "🍳", "💡" -> when (language) {
                "hi" -> "सीखना और मस्तिष्क"
                "mr" -> "शिक्षण आणि मन"
                else -> "Learning & Mind"
            }
            "💵", "📈", "💻", "💼", "✉️", "🔑", "🎯" -> when (language) {
                "hi" -> "कार्य और वित्त"
                "mr" -> "काम आणि वित्त"
                else -> "Work & Finance"
            }
            "🎨", "🎸", "🎭", "🎮", "🍿", "🎉", "🎈", "🎁", "🧸" -> when (language) {
                "hi" -> "रचनात्मकता और शौक"
                "mr" -> "सर्जनशीलता आणि छंद"
                else -> "Creativity & Hobby"
            }
            "😴", "⏰", "📅", "☀️", "🌙", "🔥", "🌈", "🧹", "🪴", "🐶" -> when (language) {
                "hi" -> "दैनिक दिनचर्या"
                "mr" -> "दैनिक दिनचर्या"
                else -> "Daily Routine"
            }
            else -> when (language) {
                "hi" -> "व्यक्तिगत"
                "mr" -> "वैयक्तिक"
                else -> "Personal"
            }
        }
    }
}

package com.example.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

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

    fun getIconForEmoji(emoji: String): ImageVector {
        return when (emoji) {
            "🏋️" -> Icons.Rounded.FitnessCenter
            "📚" -> Icons.Rounded.MenuBook
            "🧘" -> Icons.Rounded.SelfImprovement
            "💧" -> Icons.Rounded.WaterDrop
            "🏃" -> Icons.Rounded.DirectionsRun
            "✍️" -> Icons.Rounded.Edit
            "🎯" -> Icons.Rounded.Adjust
            "😴" -> Icons.Rounded.Bed
            "🎨" -> Icons.Rounded.Palette
            "💊" -> Icons.Rounded.Medication
            "🍎" -> Icons.Rounded.Restaurant
            "🧠" -> Icons.Rounded.Psychology
            "🚴" -> Icons.Rounded.DirectionsBike
            "🏊" -> Icons.Rounded.Pool
            "🚿" -> Icons.Rounded.Shower
            "🧹" -> Icons.Rounded.Brush
            "🪴" -> Icons.Rounded.LocalFlorist
            "🐶" -> Icons.Rounded.Pets
            "🍳" -> Icons.Rounded.SoupKitchen
            "🍵" -> Icons.Rounded.LocalCafe
            "💵" -> Icons.Rounded.AttachMoney
            "📈" -> Icons.Rounded.TrendingUp
            "💻" -> Icons.Rounded.Computer
            "🎸" -> Icons.Rounded.MusicNote
            "🗣️" -> Icons.Rounded.RecordVoiceOver
            "🤝" -> Icons.Rounded.Handshake
            "❤️" -> Icons.Rounded.Favorite
            "⏰" -> Icons.Rounded.Alarm
            "📅" -> Icons.Rounded.CalendarMonth
            "🧼" -> Icons.Rounded.Soap
            "🍏" -> Icons.Rounded.Restaurant
            "🍌" -> Icons.Rounded.Restaurant
            "🥗" -> Icons.Rounded.Restaurant
            "🚶" -> Icons.Rounded.DirectionsWalk
            "⚽" -> Icons.Rounded.SportsSoccer
            "🏀" -> Icons.Rounded.SportsBasketball
            "🎭" -> Icons.Rounded.TheaterComedy
            "🎮" -> Icons.Rounded.SportsEsports
            "✉️" -> Icons.Rounded.Mail
            "🔑" -> Icons.Rounded.VpnKey
            "🍿" -> Icons.Rounded.LocalMovies
            "🚗" -> Icons.Rounded.DirectionsCar
            "✈️" -> Icons.Rounded.Flight
            "☀️" -> Icons.Rounded.WbSunny
            "🌙" -> Icons.Rounded.NightsStay
            "🔥" -> Icons.Rounded.LocalFireDepartment
            "🌈" -> Icons.Rounded.Looks
            "🎈" -> Icons.Rounded.Celebration
            "🎁" -> Icons.Rounded.CardGiftcard
            "🧩" -> Icons.Rounded.Extension
            "🧸" -> Icons.Rounded.SmartToy
            "🎉" -> Icons.Rounded.Celebration
            "👑" -> Icons.Rounded.MilitaryTech
            "💎" -> Icons.Rounded.Diamond
            "🌅" -> Icons.Rounded.WbTwilight
            "🌆" -> Icons.Rounded.NightsStay
            else -> Icons.Rounded.TaskAlt
        }
    }
}


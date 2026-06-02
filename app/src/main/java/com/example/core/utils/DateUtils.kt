package com.example.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val keyFormatterLock = Any()
    private val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getDateKey(timestampMs: Long): String {
        synchronized(keyFormatterLock) {
            return keyFormatter.format(Date(timestampMs))
        }
    }

    fun getDateKey(date: Date): String {
        synchronized(keyFormatterLock) {
            return keyFormatter.format(date)
        }
    }

    fun getTodayKey(): String {
        synchronized(keyFormatterLock) {
            return keyFormatter.format(Date())
        }
    }

    fun getYesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        synchronized(keyFormatterLock) {
            return keyFormatter.format(cal.time)
        }
    }

    fun parseDateKey(key: String): Date? {
        synchronized(keyFormatterLock) {
            return try {
                keyFormatter.parse(key)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getDaysDifference(date1: Date, date2: Date): Int {
        val cal1 = Calendar.getInstance().apply { time = date1; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val cal2 = Calendar.getInstance().apply { time = date2; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val diffMs = Math.abs(cal1.timeInMillis - cal2.timeInMillis)
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getFormattedDate(dateKey: String, language: String): String {
        val date = parseDateKey(dateKey) ?: Date()
        val locale = when (language) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale.US
        }
        val pattern = when (language) {
            "hi", "mr" -> "dd MMMM yyyy"
            else -> "MMMM dd, yyyy"
        }
        return try {
            val sdf = SimpleDateFormat(pattern, locale)
            sdf.format(date)
        } catch (e: Exception) {
            dateKey
        }
    }

    fun getDayOfWeekLabel(dateKey: String, language: String): String {
        val date = parseDateKey(dateKey) ?: Date()
        val locale = when (language) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale.US
        }
        return try {
            val sdf = SimpleDateFormat("EEE", locale)
            sdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun getMonthLabel(monthIndex: Int, language: String): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, monthIndex)
        val locale = when (language) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale.US
        }
        return try {
            val sdf = SimpleDateFormat("MMMM", locale)
            sdf.format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getYearFromDateKey(dateKey: String): Int {
        val date = parseDateKey(dateKey) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.YEAR)
    }

    fun getMonthFromDateKey(dateKey: String): Int {
        val date = parseDateKey(dateKey) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.MONTH) // 0-11
    }

    fun getDayOfMonthFromDateKey(dateKey: String): Int {
        val date = parseDateKey(dateKey) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns list of Date Keys for a given month and year
     */
    fun getDateKeysForMonth(year: Int, monthIndex: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..lastDay) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            list.add(getDateKey(cal.time))
        }
        return list
    }

    fun getDayOfWeek(dateKey: String): Int {
        val date = parseDateKey(dateKey) ?: Date()
        val cal = Calendar.getInstance().apply { time = date }
        val day = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ..., Saturday = 7
        return when (day) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}

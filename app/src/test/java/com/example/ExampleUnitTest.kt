package com.example

import com.example.core.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDateUtilsConcurrency() = runBlocking {
    val jobs = List(100) {
      launch(Dispatchers.Default) {
        val today = DateUtils.getTodayKey()
        assertNotNull(today)
        val parsed = DateUtils.parseDateKey(today)
        assertNotNull(parsed)
        val formatted = DateUtils.getDateKey(parsed!!)
        assertEquals(today, formatted)
      }
    }
    jobs.forEach { it.join() }
  }
}

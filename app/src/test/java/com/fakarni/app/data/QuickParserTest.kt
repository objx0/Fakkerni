package com.fakarni.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class QuickParserTest {
    // Friday 6 November 2026, 18:40
    private val now = LocalDateTime.of(2026, 11, 6, 18, 40)

    @Test
    fun monthlyBillWithDayAndEveningTime() {
        val result = QuickParser.parse("ادفع فاتورة الإنترنت كل شهر يوم 10 الساعة 9 م", now)
        assertEquals("ادفع فاتورة الإنترنت", result.title)
        assertEquals(Recurrence.MONTHLY, result.recurrence)
        assertEquals(LocalDateTime.of(2026, 11, 10, 21, 0), result.dateTime)
        assertEquals(ReminderCategory.MONEY, result.category)
    }

    @Test
    fun tomorrowWithTanweenAndArabicIndicDigits() {
        val result = QuickParser.parse("ذكّرني بـ موعد الطبيب غداً الساعة ١٠:٣٠ ص", now)
        assertEquals("موعد الطبيب", result.title)
        assertEquals(LocalDateTime.of(2026, 11, 7, 10, 30), result.dateTime)
        assertEquals(ReminderCategory.HEALTH, result.category)
    }

    @Test
    fun relativeHoursUseNow() {
        val result = QuickParser.parse("اتصل بأمي بعد ساعتين", now)
        assertEquals("اتصل بأمي", result.title)
        assertEquals(LocalDateTime.of(2026, 11, 6, 20, 40), result.dateTime)
    }

    @Test
    fun bareAfternoonHourMovesToPm() {
        val result = QuickParser.parse("تغيير زيت السيارة الساعة 3", now)
        assertEquals("تغيير زيت السيارة", result.title)
        assertEquals(LocalDateTime.of(2026, 11, 7, 15, 0), result.dateTime)
        assertEquals(ReminderCategory.CAR, result.category)
    }

    @Test
    fun weekdayPicksNextOccurrence() {
        val result = QuickParser.parse("مراجعة اشتراك Netflix يوم الثلاثاء", now)
        assertEquals("مراجعة اشتراك Netflix", result.title)
        assertEquals(LocalDateTime.of(2026, 11, 10, 9, 0), result.dateTime)
        assertEquals(ReminderCategory.SUBSCRIPTIONS, result.category)
    }

    @Test
    fun dailyHabitWithoutDate() {
        val result = QuickParser.parse("اشرب الماء كل يوم الساعة 10 ص", now)
        assertEquals("اشرب الماء", result.title)
        assertEquals(Recurrence.DAILY, result.recurrence)
        assertEquals(LocalDateTime.of(2026, 11, 7, 10, 0), result.dateTime)
        assertEquals("💧", result.icon)
    }

    @Test
    fun englishSentence() {
        val result = QuickParser.parse("Remind me to pay rent every month on the 1st at 9am", now)
        assertEquals("pay rent", result.title)
        assertEquals(Recurrence.MONTHLY, result.recurrence)
        assertEquals(LocalDateTime.of(2026, 12, 1, 9, 0), result.dateTime)
    }

    @Test
    fun plainTitleIsLeftAlone() {
        val result = QuickParser.parse("شراء هدية لأحمد", now)
        assertEquals("شراء هدية لأحمد", result.title)
        assertNull(result.dateTime)
        assertNull(result.recurrence)
    }

    @Test
    fun habitGoalIsExtracted() {
        val result = QuickParser.parse("اشرب الماء 8 أكواب كل يوم", now)
        assertEquals("اشرب الماء", result.title)
        assertEquals(8, result.goal)
        assertEquals(Recurrence.DAILY, result.recurrence)
    }

    @Test
    fun twiceMeansTwo() {
        val result = QuickParser.parse("دواء الضغط مرتين يومياً", now)
        assertEquals("دواء الضغط", result.title)
        assertEquals(2, result.goal)
    }

    @Test
    fun tokensPointAtOriginalText() {
        val input = "ادفع الفاتورة غداً الساعة ٩ م"
        val result = QuickParser.parse(input, now)
        val words = result.tokens.map { input.substring(it.start, it.end) }
        assertEquals(listOf("غداً", "الساعة ٩ م"), words)
        assertEquals(listOf(TokenKind.DATE, TokenKind.TIME), result.tokens.map { it.kind })
    }
}

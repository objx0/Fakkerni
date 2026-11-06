package com.fakarni.app.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

enum class TokenKind { DATE, TIME, REPEAT, GOAL }

/** [start] inclusive, [end] exclusive, in the user's original text. */
data class ParsedToken(val kind: TokenKind, val start: Int, val end: Int)

/** What the quick-add field understood from a free-form sentence. */
data class ParsedReminder(
    val title: String,
    val dateTime: LocalDateTime?,
    val recurrence: Recurrence?,
    val category: ReminderCategory?,
    val icon: String?,
    /** "8 أكواب", "مرتين", "3 times" — how many times a day, for habits. */
    val goal: Int? = null,
    /** Character ranges of [ParsedReminder]'s source text that were understood. */
    val tokens: List<ParsedToken> = emptyList(),
) {
    val understoodSomething: Boolean get() = dateTime != null || recurrence != null || category != null
}

/**
 * Small rule-based parser for Arabic (MSA + Libyan colloquial) and English
 * reminder sentences, e.g. «ادفع فاتورة الإنترنت كل شهر يوم 10 الساعة 9 م».
 * Recognised date/time/repeat phrases are removed from the title; category
 * keywords are kept because they are part of what the user wants to remember.
 */
object QuickParser {
    private const val L = "(?<![\\p{L}\\d])"
    private const val R = "(?![\\p{L}\\d])"
    private val opts = setOf(RegexOption.IGNORE_CASE)
    private const val DEFAULT_HOUR = 9

    private val leadIn = Regex("^\\s*(ذكرني|فكرني|نبهني|remind me( to)?)\\s*(ب|بـ|ان|أن|اني|أني|to)?\\s+", opts)

    private val recurrenceRules = listOf(
        Regex("${L}(كل يوم|يوميا|يومي|every day|daily)$R", opts) to Recurrence.DAILY,
        Regex("${L}(كل [أا]سبوع|[أا]سبوعيا|every week|weekly)$R", opts) to Recurrence.WEEKLY,
        Regex("${L}(كل شهر|شهريا|every month|monthly)$R", opts) to Recurrence.MONTHLY,
    )

    private val relativeDuration = Regex(
        "${L}(?:بعد|in)\\s+(?:(\\d+)\\s*)?(ساعة|ساعات|ساعتين|دقيقة|دقائق|دقايق|يوم|ايام|أيام|يومين|hours?|minutes?|mins?|days?)$R",
        opts,
    )
    private val dayAfterTomorrow = Regex("${L}(بعد غد|بعد بكرة|بعد بكره|day after tomorrow)$R", opts)
    private val tomorrow = Regex("${L}(غدا|بكرة|بكره|tomorrow)$R", opts)
    private val today = Regex("${L}(اليوم|today|tonight)$R", opts)
    private val weekday = Regex(
        "${L}(?:يوم\\s+)?(?:ال)?(سبت|احد|أحد|اثنين|إثنين|ثلاثاء|اربعاء|أربعاء|خميس|جمعة|saturday|sunday|monday|tuesday|wednesday|thursday|friday)$R",
        opts,
    )
    private val dayOfMonth = Regex("${L}(?:يوم|on the|on)\\s+(\\d{1,2})(?:st|nd|rd|th)?$R", opts)
    private val clock = Regex(
        "${L}(?:(?:الساعة|الساعه|ساعة|at)\\s*)?(\\d{1,2})(?:[:.](\\d{2}))?\\s*(صباحا|صباح|الصبح|ص|مساء|المساء|م|الظهر|ظهرا|العصر|الليل|بالليل|am|pm|a\\.m\\.|p\\.m\\.)$R",
        opts,
    )
    private val clockPrefixed = Regex("${L}(?:الساعة|الساعه|at)\\s*(\\d{1,2})(?:[:.](\\d{2}))?$R", opts)
    private val partOfDay = listOf(
        Regex("${L}(الصبح|صباحا|في الصباح|morning)$R", opts) to 9,
        Regex("${L}(الظهر|ظهرا|noon)$R", opts) to 12,
        Regex("${L}(العصر|afternoon)$R", opts) to 16,
        Regex("${L}(المساء|مساء|evening)$R", opts) to 19,
        Regex("${L}(الليل|بالليل|night)$R", opts) to 21,
    )

    private val goalPattern = Regex(
        "${L}(?:(\\d{1,2})\\s*(أكواب|اكواب|كوب|كاسات|مرات|مره|مرة|times|glasses|cups)|(مرتين|twice))$R",
        opts,
    )

    private val categoryRules: List<Triple<Regex, ReminderCategory, String>> = listOf(
        Triple(Regex("(سيار|زيت|بنزين|اطار|إطار|ميكانيك|\\bcar\\b|\\boil\\b|tyre|tire)", opts), ReminderCategory.CAR, "🚗"),
        Triple(Regex("(اشتراك|نتفليكس|netflix|spotify|سبوتيفاي|يوتيوب|youtube|subscription)", opts), ReminderCategory.SUBSCRIPTIONS, "🎬"),
        Triple(Regex("(اشرب|ماء|مويه|موية|\\bwater\\b)", opts), ReminderCategory.HEALTH, "💧"),
        Triple(Regex("(دواء|حبوب|علاج|طبيب|دكتور|اسنان|أسنان|medicine|\\bpills?\\b|doctor|dentist)", opts), ReminderCategory.HEALTH, "💊"),
        Triple(Regex("(فاتور|فواتير|ادفع|أدفع|دفع|ايجار|إيجار|قسط|\\bbills?\\b|\\bpay\\b|\\brent\\b)", opts), ReminderCategory.MONEY, "💳"),
        Triple(Regex("(تسوق|اشتري|أشتري|شراء|مقاضي|groceries|\\bbuy\\b|shopping)", opts), ReminderCategory.HOME, "🛒"),
        Triple(Regex("(البيت|بيت|منزل|تنظيف|غسيل|\\bhome\\b|\\bclean)", opts), ReminderCategory.HOME, "🏠"),
    )

    fun parse(input: String, now: LocalDateTime): ParsedReminder {
        // Matching runs on a normalised copy; [origin] maps each of its chars back to [input]
        // so the editor can highlight exactly the words that were understood.
        val origin = ArrayList<Int>(input.length)
        val normalized = StringBuilder(input.length)
        input.forEachIndexed { index, c ->
            val mapped = normalizeChar(c)
            if (mapped != null) {
                normalized.append(mapped)
                origin.add(index)
            }
        }
        var text = normalized.toString()
        val originalTitleSource = text
        val tokens = mutableListOf<ParsedToken>()

        // Understood words are blanked out (same length) so indices stay aligned.
        fun take(range: IntRange, kind: TokenKind) {
            if (range.isEmpty()) return
            // Extend over trailing diacritics (e.g. the tanween in «غداً») that normalisation dropped.
            val end = if (range.last + 1 < origin.size) origin[range.last + 1] else input.length
            tokens += ParsedToken(kind, origin[range.first], end)
            text = text.replaceRange(range, " ".repeat(range.last - range.first + 1))
        }

        var recurrence: Recurrence? = null
        for ((regex, value) in recurrenceRules) {
            val match = regex.find(text) ?: continue
            recurrence = value
            take(match.range, TokenKind.REPEAT)
            break
        }

        var goal: Int? = null
        goalPattern.find(text)?.let { match ->
            val n = if (match.groupValues[3].isNotEmpty()) 2 else match.groupValues[1].toIntOrNull()
            if (n != null && n in 1..30) {
                goal = n
                take(match.range, TokenKind.GOAL)
            }
        }

        var date: LocalDate? = null
        var time: LocalTime? = null
        var exact: LocalDateTime? = null

        relativeDuration.find(text)?.let { match ->
            val unit = match.groupValues[2].lowercase()
            val explicit = match.groupValues[1].toLongOrNull()
            val amount = explicit ?: if (unit == "ساعتين" || unit == "يومين") 2L else 1L
            exact = when {
                unit.startsWith("ساع") || unit.startsWith("hour") -> now.plusHours(amount)
                unit.startsWith("دق") || unit.startsWith("min") -> now.plusMinutes(amount)
                else -> null
            }
            if (exact == null) date = now.toLocalDate().plusDays(amount)
            take(match.range, if (exact == null) TokenKind.DATE else TokenKind.TIME)
        }

        if (exact == null && date == null) {
            for ((regex, offset) in listOf(dayAfterTomorrow to 2L, tomorrow to 1L, today to 0L)) {
                val match = regex.find(text) ?: continue
                date = now.toLocalDate().plusDays(offset)
                take(match.range, TokenKind.DATE)
                break
            }
        }

        if (exact == null && date == null) {
            weekday.find(text)?.let { match ->
                val target = dayOfWeek(match.groupValues[1])
                if (target != null) {
                    val base = now.toLocalDate()
                    date = if (base.dayOfWeek == target) base else base.with(TemporalAdjusters.next(target))
                    take(match.range, TokenKind.DATE)
                }
            }
        }

        if (exact == null && date == null) {
            dayOfMonth.find(text)?.let { match ->
                val day = match.groupValues[1].toInt()
                if (day in 1..31) {
                    date = nextDayOfMonth(now.toLocalDate(), day)
                    take(match.range, TokenKind.DATE)
                }
            }
        }

        if (exact == null) {
            val clockMatch = clock.find(text)
            val prefixedMatch = if (clockMatch == null) clockPrefixed.find(text) else null
            val match = clockMatch ?: prefixedMatch
            if (match != null) {
                val hour = match.groupValues[1].toInt()
                val minute = match.groupValues[2].toIntOrNull() ?: 0
                val marker = if (clockMatch != null) match.groupValues[3].lowercase() else ""
                val h24 = to24Hour(hour, marker)
                if (h24 != null && minute in 0..59) {
                    time = LocalTime.of(h24, minute)
                    take(match.range, TokenKind.TIME)
                }
            }
            if (time == null) {
                for ((regex, hour) in partOfDay) {
                    val partMatch = regex.find(text) ?: continue
                    time = LocalTime.of(hour, 0)
                    take(partMatch.range, TokenKind.TIME)
                    break
                }
            }
        }

        val dateTime: LocalDateTime? = exact?.withSecond(0)?.withNano(0) ?: when {
            date != null -> date!!.atTime(time ?: LocalTime.of(DEFAULT_HOUR, 0))
            time != null -> {
                val candidate = now.toLocalDate().atTime(time!!)
                if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
            }
            else -> null
        }

        val match = categoryRules.firstOrNull { it.first.containsMatchIn(originalTitleSource) }
        return ParsedReminder(
            title = cleanTitle(text),
            dateTime = dateTime,
            recurrence = recurrence,
            category = match?.second,
            icon = match?.third,
            goal = goal,
            tokens = tokens.sortedBy { it.start },
        )
    }

    private fun normalizeChar(c: Char): Char? = when (c) {
        in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
        in '\u06F0'..'\u06F9' -> '0' + (c - '\u06F0')
        in '\u064B'..'\u0652', '\u0640' -> null
        else -> c
    }

    /** Arabic-Indic digits to ASCII; strips tashkeel and tatweel so «غداً» matches «غدا». */
    fun normalize(input: String): String {
        val out = StringBuilder(input.length)
        for (c in input) {
            when (c) {
                in '٠'..'٩' -> out.append('0' + (c - '٠'))
                in '۰'..'۹' -> out.append('0' + (c - '۰'))
                in 'ً'..'ْ', 'ـ' -> Unit
                else -> out.append(c)
            }
        }
        return out.toString()
    }

    private fun cleanTitle(text: String): String = text
        .replace(leadIn, "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim(' ', ',', '،', '-', '.', '؛')
        .replace(Regex("\\s+(في|على|الساعة|at|on)$", RegexOption.IGNORE_CASE), "")
        .trim()

    private fun to24Hour(hour: Int, marker: String): Int? {
        if (hour !in 0..23) return null
        val pm = marker in setOf("م", "مساء", "المساء", "pm", "p.m.", "العصر", "الليل", "بالليل") ||
            (marker in setOf("الظهر", "ظهرا") && hour < 12)
        val am = marker in setOf("ص", "صباحا", "صباح", "الصبح", "am", "a.m.")
        return when {
            pm && hour < 12 -> hour + 12
            am && hour == 12 -> 0
            pm || am -> hour
            // No marker: "الساعة 3" almost always means the afternoon.
            hour in 1..7 -> hour + 12
            else -> hour
        }
    }

    private fun dayOfWeek(word: String): DayOfWeek? = when (word.lowercase()) {
        "سبت", "saturday" -> DayOfWeek.SATURDAY
        "احد", "أحد", "sunday" -> DayOfWeek.SUNDAY
        "اثنين", "إثنين", "monday" -> DayOfWeek.MONDAY
        "ثلاثاء", "tuesday" -> DayOfWeek.TUESDAY
        "اربعاء", "أربعاء", "wednesday" -> DayOfWeek.WEDNESDAY
        "خميس", "thursday" -> DayOfWeek.THURSDAY
        "جمعة", "friday" -> DayOfWeek.FRIDAY
        else -> null
    }

    private fun nextDayOfMonth(from: LocalDate, day: Int): LocalDate {
        val thisMonth = from.withDayOfMonth(minOf(day, from.lengthOfMonth()))
        if (!thisMonth.isBefore(from)) return thisMonth
        val next = from.plusMonths(1).withDayOfMonth(1)
        return next.withDayOfMonth(minOf(day, next.lengthOfMonth()))
    }
}

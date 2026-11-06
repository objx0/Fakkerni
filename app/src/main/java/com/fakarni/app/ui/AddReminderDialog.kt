package com.fakarni.app.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakarni.app.R
import com.fakarni.app.data.ParsedToken
import com.fakarni.app.data.QuickParser
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.Reminder
import com.fakarni.app.data.ReminderCategory
import com.fakarni.app.data.TokenKind
import com.fakarni.app.ui.components.AppEmoji
import com.fakarni.app.ui.components.FakIcons
import com.fakarni.app.ui.components.InkButton
import com.fakarni.app.ui.components.categoryGradient
import com.fakarni.app.ui.components.formatClock
import com.fakarni.app.ui.components.glass
import com.fakarni.app.ui.theme.LocalGlass
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private val ICONS = listOf("✨", "🚗", "🎬", "💧", "💊", "🚶", "📖", "🏠", "💳", "🛠️", "📅", "🛒")
private val LEAD_OPTIONS = listOf(0, 10, 60, 1440)

// Token colours from the concept: violet repeat, teal day, orange time.
private val RepeatInk = Color(0xFF4B2FE0)
private val DayInk = Color(0xFF0E7490)
private val TimeInk = Color(0xFFC2410C)
private val GoalInk = Color(0xFFB0185A)

/**
 * Quick-add / edit sheet from the Liquid Glass concept: one sentence in, the
 * understood parts highlighted inline and summarised as tappable chips.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun AddReminderDialog(
    initial: Reminder? = null,
    startAsHabit: Boolean = false,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (Reminder) -> Unit,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val zone = ZoneId.systemDefault()
    val glass = LocalGlass.current
    val colors = MaterialTheme.colorScheme
    val isNew = initial == null

    var text by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var showNote by remember(initial?.id) { mutableStateOf(!initial?.note.isNullOrBlank()) }
    var manualIcon by remember(initial?.id) { mutableStateOf(initial?.icon ?: "✨") }
    var manualDate by remember(initial?.id) { mutableLongStateOf(initial?.date ?: defaultTime()) }
    var manualRecurrence by remember(initial?.id) { mutableStateOf(initial?.recurrence ?: Recurrence.NONE) }
    var manualCategory by remember(initial?.id) { mutableStateOf(initial?.category ?: ReminderCategory.PERSONAL) }
    var manualHabit by remember(initial?.id) { mutableStateOf(initial?.isHabit ?: startAsHabit) }
    var manualGoal by remember(initial?.id) { mutableIntStateOf(initial?.dailyGoal ?: if (startAsHabit) 8 else 1) }
    var lead by remember(initial?.id) { mutableIntStateOf(initial?.leadMinutes ?: 0) }
    var iconTouched by remember(initial?.id) { mutableStateOf(!isNew) }
    var dateTouched by remember(initial?.id) { mutableStateOf(!isNew) }
    var recurrenceTouched by remember(initial?.id) { mutableStateOf(!isNew) }
    var categoryTouched by remember(initial?.id) { mutableStateOf(!isNew) }
    var habitTouched by remember(initial?.id) { mutableStateOf(!isNew || startAsHabit) }
    var goalTouched by remember(initial?.id) { mutableStateOf(!isNew || startAsHabit) }
    var menu by remember { mutableStateOf<String?>(null) }

    val parsed = remember(text) { QuickParser.parse(text, LocalDateTime.now(zone)) }
    val autoDate = !dateTouched && parsed.dateTime != null
    val autoRecurrence = !recurrenceTouched && parsed.recurrence != null
    val autoCategory = !categoryTouched && parsed.category != null
    val autoIcon = !iconTouched && parsed.icon != null
    val autoHabit = !habitTouched && (parsed.goal != null || (parsed.recurrence == Recurrence.DAILY && parsed.category == ReminderCategory.HEALTH))
    val autoGoal = !goalTouched && parsed.goal != null

    val date = if (autoDate) parsed.dateTime!!.atZone(zone).toInstant().toEpochMilli() else manualDate
    val recurrence = if (autoRecurrence) parsed.recurrence!! else manualRecurrence
    val category = if (autoCategory) parsed.category!! else manualCategory
    val icon = if (autoIcon) parsed.icon!! else manualIcon
    val isHabit = autoHabit || manualHabit
    val goal = if (autoGoal) parsed.goal!! else manualGoal
    val understood = isNew && (autoDate || autoRecurrence || autoCategory || autoHabit || autoGoal)
    val title = (if (understood && parsed.title.isNotBlank()) parsed.title else text).trim()

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (isNew) {
            delay(250)
            runCatching { focus.requestFocus() }
        }
    }

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) text = if (text.isBlank()) spoken else "$text $spoken"
        }
    }
    val speechPrompt = stringResource(R.string.quick_add_title)

    fun pickDate() {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        DatePickerDialog(context, { _, year, month, day ->
            manualDate = Calendar.getInstance().apply { timeInMillis = date; set(year, month, day) }.timeInMillis
            dateTouched = true
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun pickTime() {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        TimePickerDialog(context, { _, hour, minute ->
            manualDate = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            dateTouched = true
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)).show()
    }

    fun save() {
        if (title.isBlank()) return
        onSave(
            Reminder(
                id = initial?.id ?: System.currentTimeMillis(),
                title = title,
                note = note.trim(),
                date = date,
                icon = icon,
                done = initial?.done ?: false,
                recurrence = if (isHabit && recurrence == Recurrence.NONE) Recurrence.DAILY else recurrence,
                category = category,
                recurrenceAnchorDay = Instant.ofEpochMilli(date).atZone(zone).dayOfMonth,
                isHabit = isHabit,
                dailyGoal = if (isHabit) goal else 1,
                leadMinutes = lead,
            ),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        // Floating glass panel, inset from the screen edges like the concept.
        Column(
            Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                .imePadding()
                .sheetSurface(RoundedCornerShape(40.dp))
                .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(5.dp).clip(CircleShape).background(colors.onSurface.copy(alpha = .2f)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(if (isNew) R.string.quick_add_title else R.string.edit_reminder),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (!isNew && onDelete != null) RoundIcon(FakIcons.Trash, stringResource(R.string.delete), tint = colors.error, onClick = onDelete)
                RoundIcon(FakIcons.Close, stringResource(R.string.cancel), onClick = onDismiss)
            }

            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val hint = stringResource(R.string.quick_add_hint)
                val highlight = remember(parsed.tokens, isNew) { TokenHighlight(if (isNew) parsed.tokens else emptyList()) }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp,
                        lineHeight = 36.sp,
                        color = colors.onSurface,
                    ),
                    cursorBrush = SolidColor(colors.primary),
                    visualTransformation = highlight,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .focusRequester(focus)
                        .semantics { contentDescription = hint },
                    decorationBox = { field ->
                        // The whole box is the field, so tapping anywhere on the hint focuses it.
                        Box(Modifier.fillMaxWidth().heightIn(min = 80.dp)) {
                            if (text.isEmpty()) Text(
                                hint,
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily, fontWeight = FontWeight.Medium, lineHeight = 32.sp),
                                color = colors.onSurfaceVariant.copy(alpha = .7f),
                            )
                            field()
                        }
                    },
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(if (understood) R.string.understood_tap_to_change else R.string.details))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailChip(FakIcons.Calendar, DayInk, formatDay(date, locale)) { pickDate() }
                        DetailChip(FakIcons.Clock, TimeInk, formatClock(date, locale)) { pickTime() }
                        Box {
                            DetailChip(FakIcons.Repeat, RepeatInk, stringResource((if (isHabit && recurrence == Recurrence.NONE) Recurrence.DAILY else recurrence).labelResource())) { menu = "repeat" }
                            DropdownMenu(expanded = menu == "repeat", onDismissRequest = { menu = null }) {
                                Recurrence.entries.forEach { item ->
                                    DropdownMenuItem(text = { Text(stringResource(item.labelResource())) }, onClick = {
                                        manualRecurrence = item
                                        recurrenceTouched = true
                                        menu = null
                                    })
                                }
                            }
                        }
                        Box {
                            DetailChip(null, categoryGradient(category).last(), stringResource(category.labelResource())) { menu = "category" }
                            DropdownMenu(expanded = menu == "category", onDismissRequest = { menu = null }) {
                                ReminderCategory.entries.forEach { item ->
                                    DropdownMenuItem(text = { Text(stringResource(item.labelResource())) }, onClick = {
                                        manualCategory = item
                                        categoryTouched = true
                                        menu = null
                                    })
                                }
                            }
                        }
                        Box {
                            DetailChip(FakIcons.Flame, GoalInk, if (isHabit) stringResource(R.string.habit_goal_chip, goal) else stringResource(R.string.type_reminder)) { menu = "habit" }
                            DropdownMenu(expanded = menu == "habit", onDismissRequest = { menu = null }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.type_reminder)) }, onClick = {
                                    manualHabit = false; habitTouched = true; menu = null
                                })
                                DropdownMenuItem(text = { Text(stringResource(R.string.type_habit)) }, onClick = {
                                    manualHabit = true; habitTouched = true
                                })
                                if (isHabit) Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.daily_goal), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 8.dp))
                                    RoundIcon(FakIcons.Minus, stringResource(R.string.decrease), enabled = goal > 1) {
                                        manualGoal = (goal - 1).coerceAtLeast(1); goalTouched = true
                                    }
                                    Text("$goal", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleMedium)
                                    RoundIcon(FakIcons.Plus, stringResource(R.string.increase), enabled = goal < 30) {
                                        manualGoal = (goal + 1).coerceAtMost(30); goalTouched = true
                                    }
                                }
                            }
                        }
                        Box {
                            DetailChip(null, null, null, leading = { AppEmoji(icon, size = 20.dp) }) { menu = "icon" }
                            DropdownMenu(expanded = menu == "icon", onDismissRequest = { menu = null }) {
                                ICONS.chunked(4).forEach { row ->
                                    Row(Modifier.padding(horizontal = 8.dp)) {
                                        row.forEach { item ->
                                            Box(
                                                Modifier.size(48.dp).clip(CircleShape)
                                                    .background(if (item == icon) colors.primary.copy(alpha = .14f) else Color.Transparent)
                                                    .semantics { selected = item == icon }
                                                    .clickable(role = Role.RadioButton) {
                                                        manualIcon = item; iconTouched = true; menu = null
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) { AppEmoji(item, size = 26.dp) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.alert_before))
                    LeadSegmented(selected = lead, onSelect = { lead = it })
                }

                if (isNew) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.suggestions))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(R.string.suggestion_water, R.string.suggestion_medicine, R.string.suggestion_bill).forEach { res ->
                            val suggestion = stringResource(res)
                            SuggestionChip(suggestion) { text = suggestion }
                        }
                    }
                }

                if (showNote) {
                    val noteLabel = stringResource(R.string.optional_note)
                    val shape = RoundedCornerShape(20.dp)
                    BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = noteLabel },
                        decorationBox = { field ->
                            Box(
                                Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(shape).background(colors.onSurface.copy(alpha = .05f), shape)
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                if (note.isEmpty()) Text(noteLabel, color = colors.onSurfaceVariant)
                                field()
                            }
                        },
                    )
                } else {
                    Text(
                        stringResource(R.string.add_note),
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(role = Role.Button) { showNote = true }.padding(vertical = 12.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape)
                        .background(colors.primary.copy(alpha = .14f), CircleShape)
                        .border(1.dp, colors.primary.copy(alpha = .25f), CircleShape)
                        .clickable(role = Role.Button) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (locale.language == "en") "en-US" else "ar")
                                .putExtra(RecognizerIntent.EXTRA_PROMPT, speechPrompt)
                            try {
                                speech.launch(intent)
                            } catch (_: ActivityNotFoundException) {
                                android.widget.Toast.makeText(context, R.string.voice_unavailable, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .semantics { contentDescription = speechPrompt },
                    contentAlignment = Alignment.Center,
                ) { Icon(FakIcons.Mic, contentDescription = null, tint = colors.primary) }
                InkButton(onClick = ::save, enabled = title.isNotBlank(), modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(if (isNew) R.string.add_reminder_action else R.string.update),
                        color = glass.onInk,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

/**
 * Nearly opaque glass for the sheet: there is no real backdrop blur on Android,
 * so a see-through panel would show the screen's text through the form.
 */
@Composable
private fun Modifier.sheetSurface(shape: RoundedCornerShape): Modifier {
    val glass = LocalGlass.current
    val surface = MaterialTheme.colorScheme.surface
    val shadow = glass.shadow.copy(alpha = if (glass.isDark) .55f else .25f)
    return this
        .shadow(24.dp, shape, ambientColor = shadow, spotColor = shadow)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(surface, surface.copy(alpha = .97f))), shape)
        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .04f), Color.Transparent)), shape)
        .border(1.dp, Brush.verticalGradient(listOf(glass.highlight, glass.edge.copy(alpha = glass.edge.alpha * .4f))), shape)
}

/** Colours the understood words inline, like the concept's highlighted sentence. */
private class TokenHighlight(private val tokens: List<ParsedToken>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (tokens.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val styled = buildAnnotatedString {
            append(text)
            tokens.forEach { token ->
                if (token.end <= text.length) {
                    val ink = when (token.kind) {
                        TokenKind.REPEAT -> RepeatInk
                        TokenKind.DATE -> DayInk
                        TokenKind.TIME -> TimeInk
                        TokenKind.GOAL -> GoalInk
                    }
                    addStyle(SpanStyle(color = ink, background = ink.copy(alpha = .13f), fontWeight = FontWeight.SemiBold), token.start, token.end)
                }
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun RoundIcon(icon: ImageVector, description: String, tint: Color = MaterialTheme.colorScheme.onSurface, enabled: Boolean = true, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .07f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = if (enabled) tint else tint.copy(alpha = .35f), modifier = Modifier.size(18.dp))
        }
    }
}

/** White chip with a coloured icon — the concept's "understood" chips. */
@Composable
private fun DetailChip(
    icon: ImageVector?,
    ink: Color?,
    label: String?,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .height(44.dp)
            .shadow(4.dp, shape, ambientColor = glass.shadow.copy(alpha = .3f), spotColor = glass.shadow.copy(alpha = .3f))
            .clip(shape)
            .background(glass.fillStrongTop, shape)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .1f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when {
            leading != null -> leading()
            icon != null && ink != null -> Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(16.dp))
            ink != null -> Box(Modifier.size(10.dp).clip(CircleShape).background(ink))
        }
        if (label != null) Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun LeadSegmented(selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlass.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.onSurface.copy(alpha = .06f)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        LEAD_OPTIONS.forEach { minutes ->
            val isSelected = minutes == selected
            val shape = RoundedCornerShape(18.dp)
            Box(
                Modifier.weight(1f).height(40.dp)
                    .then(if (isSelected) Modifier.shadow(4.dp, shape, ambientColor = glass.shadow, spotColor = glass.shadow).clip(shape).background(glass.fillStrongTop.copy(alpha = 1f), shape) else Modifier.clip(shape))
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(minutes) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(
                        when (minutes) {
                            0 -> R.string.lead_on_time
                            10 -> R.string.lead_10_minutes
                            60 -> R.string.lead_hour
                            else -> R.string.lead_day
                        },
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    val glass = LocalGlass.current
    val dash = MaterialTheme.colorScheme.onSurface.copy(alpha = .25f)
    Box(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(glass.fillTop)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(size.height / 2),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 1) }
}

private fun defaultTime(): Long = System.currentTimeMillis() + 3_600_000L

private fun formatDay(time: Long, locale: Locale): String =
    DateTimeFormatter.ofPattern("EEE d MMMM", locale).format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()))

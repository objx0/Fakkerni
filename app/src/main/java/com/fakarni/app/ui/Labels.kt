package com.fakarni.app.ui

import androidx.annotation.StringRes
import com.fakarni.app.R
import com.fakarni.app.data.Recurrence
import com.fakarni.app.data.ReminderCategory

@StringRes
fun Recurrence.labelResource(): Int = when (this) {
    Recurrence.NONE -> R.string.recurrence_none
    Recurrence.DAILY -> R.string.recurrence_daily
    Recurrence.WEEKLY -> R.string.recurrence_weekly
    Recurrence.MONTHLY -> R.string.recurrence_monthly
}

@StringRes
fun ReminderCategory.labelResource(): Int = when (this) {
    ReminderCategory.PERSONAL -> R.string.category_personal
    ReminderCategory.CAR -> R.string.category_car
    ReminderCategory.HEALTH -> R.string.category_health
    ReminderCategory.SUBSCRIPTIONS -> R.string.category_subscriptions
    ReminderCategory.HOME -> R.string.category_home
    ReminderCategory.MONEY -> R.string.category_money
}

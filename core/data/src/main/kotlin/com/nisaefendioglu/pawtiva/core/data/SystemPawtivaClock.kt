package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Device clock and current time zone; read on every call so zone changes apply immediately. */
internal class SystemPawtivaClock @Inject constructor() : PawtivaClock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

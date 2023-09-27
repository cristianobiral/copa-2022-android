package me.dio.copa.catar.notification.scheduler.extensions

import android.content.Context
import androidx.work.*
import me.dio.copa.catar.domain.model.MatchDomain
import java.time.Duration
import java.time.LocalDateTime

private const val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE_KEY"
private const val NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT_KEY"

class NotificationMatchesWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {

        val title = inputData.getString(NOTIFICATION_TITLE_KEY)
            ?: throw IllegalArgumentException("title is required")
        val content = inputData.getString(NOTIFICATION_CONTENT_KEY)
            ?: throw IllegalArgumentException("content is required")

        context.showNotification(title, content)

        return Result.success()
    }

    companion object {

        fun start(context: Context, match: MatchDomain) {

            val (id, _, _, team1, team2, date) = match

            val initialDelay = Duration.between(LocalDateTime.now(), date)
                .minusMinutes(15)

            val inputData = workDataOf(
                NOTIFICATION_TITLE_KEY to "Se prepare que o jogo vai começar",
                NOTIFICATION_CONTENT_KEY to "Hoje tem ${team1.flag} x ${team2.flag}"
            )

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    id,
                    ExistingWorkPolicy.KEEP,
                    createRequest(initialDelay, inputData)
                )

        }

        fun cancel(context: Context, match: MatchDomain) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(match.id)
        }

        private fun createRequest(initialDelay: Duration, inputData: Data): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<NotificationMatchesWorker>()
                .setInitialDelay(initialDelay)
                .setInputData(inputData)
                .build()

    }

}
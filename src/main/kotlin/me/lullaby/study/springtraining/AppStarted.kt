package me.lullaby.study.springtraining

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class AppStarted(
    val jobLauncher: JobLauncher,
    val job: Job
): ApplicationListener<ApplicationStartedEvent> {

    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        val jobParametersBuilder = JobParametersBuilder()
        jobLauncher.run(job, jobParametersBuilder.toJobParameters())
    }

}

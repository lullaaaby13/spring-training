package me.lullaby.study.springtraining

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.builder.TaskletStepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource


@Configuration
class SimpleJobConfiguration (
    val jobRepository: JobRepository,
    val tasklet: SimpleTasklet,
    val platformTransactionManager: PlatformTransactionManager
){
    val log: Logger = LoggerFactory.getLogger(SimpleJobConfiguration::class.java)

    @Bean
    @Throws(Exception::class)
    fun jobLauncher(): JobLauncher? {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }

    @Bean
    fun simpleJob(context: ApplicationContext): Job {
        return JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1())
            .next(simpleStep2())
            .build()
    }

    @Bean
    fun simpleStep1(): Step {
        return TaskletStepBuilder(StepBuilder("simpleStep1", jobRepository))
            .tasklet(tasklet, platformTransactionManager)
            .build()
    }

    @Bean
    fun simpleStep2(): Step {
        return StepBuilder("simpleStep2", jobRepository)
            .tasklet({ contribution, chunkContext ->
                log.info("This is SimpleStep2")
                RepeatStatus.FINISHED
            }, platformTransactionManager).build()
    }

}

@Component
@StepScope
class SimpleTasklet: Tasklet {

    val logger: Logger = LoggerFactory.getLogger(SimpleTasklet::class.java)

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        logger.info("simple task is executed.")
        return RepeatStatus.FINISHED
    }

}

@Configuration
@EnableTransactionManagement
class PersistenceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa")
    fun jpaProperties(): JpaProperties {
        return JpaProperties()
    }

    @Bean
    fun entityManagerFactory(builder: EntityManagerFactoryBuilder): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource())
            .properties(jpaProperties().properties)
            .packages("me.lullaby.study")
            .persistenceUnit("default")
            .build()
    }

    @Bean
    fun transactionManager(entityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        val jpaTransactionManager = JpaTransactionManager(entityManagerFactory.`object`!!)
        jpaTransactionManager.dataSource = dataSource()
        return jpaTransactionManager
    }
}

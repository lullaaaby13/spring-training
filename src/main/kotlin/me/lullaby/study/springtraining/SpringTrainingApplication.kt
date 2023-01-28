package me.lullaby.study.springtraining

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableBatchProcessing(dataSourceRef = "dataSource", transactionManagerRef = "transactionManager")
@SpringBootApplication
class SpringTrainingApplication

fun main(args: Array<String>) {
    runApplication<SpringTrainingApplication>(*args)
}

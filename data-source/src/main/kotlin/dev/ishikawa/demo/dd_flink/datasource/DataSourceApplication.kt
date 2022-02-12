package dev.ishikawa.demo.dd_flink.datasource

import dev.ishikawa.demo.dd_flink.datasource.service.DataConsumingService
import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DataSourceApplication

fun main(args: Array<String>) {
    val context = runApplication<DataSourceApplication>(*args)

    runBlocking {
        val publisher = context.getBean(DataPublishingService::class.java)
        val job1 = publisher.execute()
        val consumer = context.getBean(DataConsumingService::class.java)
        val job2 = consumer.execute()

        listOf(job1, job2).awaitAll()
    }
}

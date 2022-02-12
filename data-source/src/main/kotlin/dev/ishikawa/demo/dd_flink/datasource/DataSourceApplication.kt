package dev.ishikawa.demo.dd_flink.datasource

import dev.ishikawa.demo.dd_flink.datasource.service.DataPublishingService
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class DataSourceApplication

fun main(args: Array<String>) {
    val context = runApplication<DataSourceApplication>(*args)

    runBlocking {
        val bean = context.getBean(DataPublishingService::class.java)
        bean.execute()
    }
}

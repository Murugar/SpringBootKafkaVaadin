package com.iqmsoft.kafka.vaadin

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component



interface KafkaConnectorListener {
    fun chatMessage(user: String, message: String)
}

@Component
class KafkaConnector {

    val listeners = mutableListOf<KafkaConnectorListener>()

    fun addListener(listener: KafkaConnectorListener) {
        listeners += listener
    }

    fun removeListener(listener: KafkaConnectorListener) {
        listeners -= listener
    }

    @Suppress("SpringKotlinAutowiring")
    @Autowired
    lateinit var kafka: KafkaTemplate<String, String>

    fun send(user: String, message: String) {
        log.info("$user Sending Message \"$message\"")
        kafka.send("chat", user, message)
    }

    @KafkaListener(topics = arrayOf("chat"))
    fun receive(consumerRecord: ConsumerRecord<String?, String?>) {
        val key: String = consumerRecord.key() ?: "???"
        val value: String = consumerRecord.value() ?: "???"
        log.info("Got Kafka Record with Key \"$key\" and value \"$value\"")
        listeners.forEach { listener -> listener.chatMessage(key, value) }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(KafkaConnector::class.java)
    }
}

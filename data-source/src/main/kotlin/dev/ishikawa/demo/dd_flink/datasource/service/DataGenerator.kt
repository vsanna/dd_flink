package dev.ishikawa.demo.dd_flink.datasource.service

import dev.ishikawa.demo.dd_flink.PUserActivityEvent
import dev.ishikawa.demo.dd_flink.PUserActivityEventKt.pUserActivity
import dev.ishikawa.demo.dd_flink.PUserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserAction
import dev.ishikawa.demo.dd_flink.datasource.configs.UserActionType
import dev.ishikawa.demo.dd_flink.datasource.configs.UserActivityEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserProfileEvent
import dev.ishikawa.demo.dd_flink.datasource.configs.UserProfileEventType
import dev.ishikawa.demo.dd_flink.pUserActivityEvent
import dev.ishikawa.demo.dd_flink.pUserProfileEvent
import java.time.Instant
import java.util.*
import kotlin.random.Random

object DataGenerator {
    private val userIds = mutableSetOf<String>()
    private val randomDiceForUserProfileEvent = Random(100)
    private val randomDiceForUserActivityEvent = Random(200)

    init {
        // initial users
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
        userIds.add(UUID.randomUUID().toString())
    }

    fun genUserProfileEvent(): UserProfileEvent {
        val dice = randomDiceForUserProfileEvent.nextInt(0, 10000) % UserProfileEventType.values().size

        if(userIds.isEmpty()) {
            userIds.add(UUID.randomUUID().toString())
        }

        return when(dice) {
            0 -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString().also { userIds.add(it) },
                type = UserProfileEventType.CREATION,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
            1 -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = userIds.random(),
                type = UserProfileEventType.UPDATE,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
            else -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = userIds.random().also { userIds.remove(it) },
                type = UserProfileEventType.DELETION,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
        }
    }

    fun genUserActivityEvent() : UserActivityEvent {
        if(userIds.isEmpty()) {
            userIds.add(UUID.randomUUID().toString())
        }
        val dice = randomDiceForUserActivityEvent.nextInt(0, 10000) % UserActionType.values().size
        val action = when(dice) {
            0 -> UserAction(
                actionType = UserActionType.CLICK_URL,
                data = "{}"
            )
            1 -> UserAction(
                actionType = UserActionType.PAGE_BACK,
                data = "{}"
            )
            2 -> UserAction(
                actionType = UserActionType.PAGE_FORWARD,
                data = "{}"
            )
            3 -> UserAction(
                actionType = UserActionType.SCROLL_TO_TOP,
                data = "{}"
            )
            else -> UserAction(
                actionType = UserActionType.SCROLL_TO_BOTTOM,
                data = "{}"
            )
        }
        return UserActivityEvent(
            eventId = UUID.randomUUID().toString(),
            userId = userIds.random(),
            action = action,
            ts = Instant.now().toEpochMilli()
        )
    }


    fun genUserProfileProtobufEvent(): PUserProfileEvent {
        val dice = randomDiceForUserProfileEvent.nextInt(0, 10000) % UserProfileEventType.values().size

        if(userIds.isEmpty()) {
            userIds.add(UUID.randomUUID().toString())
        }

        return when(dice) {
            0 -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = UUID.randomUUID().toString().also { userIds.add(it) }
                type = PUserProfileEvent.PUserProfileEventType.CREATION
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
            1 -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = userIds.random()
                type = PUserProfileEvent.PUserProfileEventType.UPDATE
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
            else -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = userIds.random().also { userIds.remove(it) }
                type = PUserProfileEvent.PUserProfileEventType.DELETION
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
        }
    }

    fun genUserActivityProtobufEvent() : PUserActivityEvent {
        if(userIds.isEmpty()) {
            userIds.add(UUID.randomUUID().toString())
        }
        val dice = randomDiceForUserActivityEvent.nextInt(0, 10000) % UserActionType.values().size

        val action = when(dice) {
            0 -> pUserActivity {
                actionType = PUserActivityEvent.PUserActivity.UserActionType.CLICK_URL
                data = "{}"
            }
            1 -> pUserActivity {
                actionType = PUserActivityEvent.PUserActivity.UserActionType.PAGE_BACK
                data = "{}"
            }
            2 -> pUserActivity {
                actionType = PUserActivityEvent.PUserActivity.UserActionType.PAGE_FORWARD
                data = "{}"
            }
            3 -> pUserActivity {
                actionType = PUserActivityEvent.PUserActivity.UserActionType.SCROLL_TO_TOP
                data = "{}"
            }
            else -> pUserActivity {
                actionType = PUserActivityEvent.PUserActivity.UserActionType.SCROLL_TO_BOTTOM
                data = "{}"
            }
        }
        return pUserActivityEvent {
            eventId = UUID.randomUUID().toString()
            userId = userIds.random()
            this.action = action
            ts = Instant.now().toEpochMilli()
        }
    }
}
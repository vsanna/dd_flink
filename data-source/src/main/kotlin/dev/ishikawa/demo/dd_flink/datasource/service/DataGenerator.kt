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
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object DataGenerator {
    // use userIdMap as kind of "concurrent set"
    private val userIdMap = ConcurrentHashMap<String, Boolean>();
    private val randomDiceForUserProfileEvent = Random(100)
    private val randomDiceForUserActivityEvent = Random(200)

    init {
        // initial users
        (0..10).forEach { genUser() }
    }

    fun genUserProfileEvent(): UserProfileEvent {
        val dice = randomDiceForUserProfileEvent.nextInt(0, 10000) % UserProfileEventType.values().size

        if(userIdMap.keys.isEmpty()) genUser()

        return when(dice) {
            0 -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = genUser(),
                type = UserProfileEventType.CREATION,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
            1 -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = getRandomUser(),
                type = UserProfileEventType.UPDATE,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
            else -> UserProfileEvent(
                eventId = UUID.randomUUID().toString(),
                userId = delRandomUser(),
                type = UserProfileEventType.DELETION,
                data = "{}",
                ts = Instant.now().toEpochMilli()
            )
        }
    }

    fun genUserActivityEvent() : UserActivityEvent {
        if(userIdMap.keys.isEmpty()) genUser()

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
            userId = getRandomUser(),
            action = action,
            ts = Instant.now().toEpochMilli()
        )
    }


    fun genUserProfileProtobufEvent(): PUserProfileEvent {
        val dice = randomDiceForUserProfileEvent.nextInt(0, 10000) % UserProfileEventType.values().size

        if(userIdMap.keys.isEmpty()) genUser()

        return when(dice) {
            0 -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = genUser()
                type = PUserProfileEvent.PUserProfileEventType.CREATION
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
            1 -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = getRandomUser()
                type = PUserProfileEvent.PUserProfileEventType.UPDATE
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
            else -> pUserProfileEvent {
                eventId = UUID.randomUUID().toString()
                userId = delRandomUser()
                type = PUserProfileEvent.PUserProfileEventType.DELETION
                data = "{}"
                ts = Instant.now().toEpochMilli()
            }
        }
    }

    fun genUserActivityProtobufEvent() : PUserActivityEvent {
        if(userIdMap.keys.isEmpty()) genUser()

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
            userId = getRandomUser()
            this.action = action
            ts = Instant.now().toEpochMilli()
        }
    }

    private fun getRandomUser(): String {
        return userIdMap.keys.random()
    }

    private fun genUser(): String {
        return UUID.randomUUID().toString()
            .also { addUser(it) }
    }

    private fun addUser(id: String): String {
        userIdMap[id] = true
        return id
    }

    private fun delRandomUser(): String {
        return getRandomUser()
            .also { delUser(it) }
    }

    private fun delUser(id: String): String {
        userIdMap.remove(id)
        return id
    }
}
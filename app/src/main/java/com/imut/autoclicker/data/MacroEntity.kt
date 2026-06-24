package com.imut.autoclicker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.imut.autoclicker.gesture.GesturePoint
import com.imut.autoclicker.gesture.GestureSequence
import com.imut.autoclicker.gesture.GestureType
import com.imut.autoclicker.gesture.LoopMode

/**
 * Room entity representing a saved macro (gesture sequence).
 */
@Entity(tableName = "macros")
@TypeConverters(Converters::class)
data class MacroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val points: List<GesturePoint>,
    val loopMode: String = "INFINITE",
    val loopCount: Int = 1,
    val delayBetweenPoints: Long = 500L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0,
    val useCount: Int = 0
) {
    fun toSequence(): GestureSequence {
        return GestureSequence(
            points = points,
            loopMode = try { LoopMode.valueOf(loopMode) } catch (e: Exception) { LoopMode.INFINITE },
            loopCount = loopCount,
            delayBetweenPoints = delayBetweenPoints
        )
    }

    companion object {
        fun fromSequence(name: String, sequence: GestureSequence): MacroEntity {
            return MacroEntity(
                name = name,
                points = sequence.points,
                loopMode = sequence.loopMode.name,
                loopCount = sequence.loopCount,
                delayBetweenPoints = sequence.delayBetweenPoints
            )
        }
    }
}

/**
 * Type converters for Room database.
 */
class Converters {
    @TypeConverter
    fun fromGesturePointList(value: List<GesturePoint>): String {
        return value.joinToString(";") { point ->
            "${point.id}|${point.x}|${point.y}|${point.type.name}|${point.endX}|${point.endY}|${point.duration}|${point.delayBefore}|${point.repeatCount}"
        }
    }

    @TypeConverter
    fun toGesturePointList(value: String): List<GesturePoint> {
        if (value.isBlank()) return emptyList()
        return value.split(";").map { str ->
            val parts = str.split("|")
            GesturePoint(
                id = parts[0].toIntOrNull() ?: 0,
                x = parts[1].toFloatOrNull() ?: 0f,
                y = parts[2].toFloatOrNull() ?: 0f,
                type = try { GestureType.valueOf(parts[3]) } catch (e: Exception) { GestureType.TAP },
                endX = parts[4].toFloatOrNull() ?: 0f,
                endY = parts[5].toFloatOrNull() ?: 0f,
                duration = parts[6].toLongOrNull() ?: 100L,
                delayBefore = parts[7].toLongOrNull() ?: 0L,
                repeatCount = parts[8].toIntOrNull() ?: 1
            )
        }
    }
}

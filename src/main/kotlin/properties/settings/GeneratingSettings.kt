package properties.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.Serializable

@Serializable(with = GeneratingSettingsSerializer::class)
class GeneratingSettings(
    maxContextLength: Int = 1600,
    maxLength: Int = 200,
    repPen: Float = 1.08F,
    repPenRange: Int = 1024,
    repPenSlope: Float = 0.9F,
    temperature: Float = 0.65F,
    tfs: Float = 0.65F,
    topA: Float = 0.9F,
    topK: Int = 0,
    topP: Float = 0.9F,
    minP: Float = 0.1F,
    typical: Float = 1F,
    samplerOrder: SnapshotStateList<Int> = mutableStateListOf(/*6, 0, 1, 2, 3, 4, 5*/ 6, 0, 1, 3, 4, 2, 5),
    seed: Int = -1,
) {
    var maxContextLength: Int by mutableStateOf(maxContextLength)
    var maxLength: Int by mutableStateOf(maxLength)
    var repPen: Float by mutableStateOf(repPen)
    var repPenRange: Int by mutableStateOf(repPenRange)
    var repPenSlope: Float by mutableStateOf(repPenSlope)
    var temperature: Float by mutableStateOf(temperature)
    var tfs: Float by mutableStateOf(tfs)
    var topA: Float by mutableStateOf(topA)
    var topK: Int by mutableStateOf(topK)
    var topP: Float by mutableStateOf(topP)
    var minP: Float by mutableStateOf(minP)
    var typical: Float by mutableStateOf(typical)
    var samplerOrder: SnapshotStateList<Int> by mutableStateOf(samplerOrder)
    var seed: Int by mutableStateOf(seed)

    val maxContextLengthValueRange get() = 512F..16384F
    val maxLengthValueRange get() = 16F..1024F
    val repPenValueRange get() = 1F..1.5F
    val repPenRangeValueRange get() = 0F..16384F
    val repPenSlopeValueRange get() = 0.1F..10F
    val temperatureValueRange get() = 0.1F..4.0F
    val tfsValueRange get() = 0.0F..1.0F
    val topAValueRange get() = 0.0F..1.0F
    val topKValueRange get() = 0F..100F
    val topPValueRange get() = 0.0F..1.0F
    val minPValueRange get() = 0.0F..1.0F
    val typicalValueRange get() = 0.0F..1.0F

    fun copy(): GeneratingSettings {
        return GeneratingSettings(
            maxContextLength,
            maxLength,
            repPen,
            repPenRange,
            repPenSlope,
            temperature,
            tfs,
            topA,
            topK,
            topP,
            minP,
            typical,
            samplerOrder,
            seed,
        )
    }
}

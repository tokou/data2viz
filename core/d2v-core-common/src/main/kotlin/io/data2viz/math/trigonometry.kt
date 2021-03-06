package io.data2viz.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

const val EPSILON = 1e-6
const val EPSILON2 = EPSILON * EPSILON

const val PI = kotlin.math.PI
const val HALFPI = PI / 2.0
const val QUARTERPI = PI / 4.0

const val TAU = PI * 2.0
const val TAU_EPSILON = TAU - EPSILON

val PI_ANGLE = Angle(kotlin.math.PI)
val HALFPI_ANGLE = PI_ANGLE / 2
val TAU_ANGLE = PI_ANGLE * 2

private val DEG_TO_RAD = kotlin.math.PI / 180
private val RAD_TO_DEG = 180 / kotlin.math.PI

/**
 * Assuming this represents a value in degrees, converts the value to radians.
 */
fun Double.toRadians() = this * DEG_TO_RAD

/**
 * Assuming this represents a value in radians, converts the value to degrees.
 */
fun Double.toDegrees() = this * RAD_TO_DEG

val Number.deg:Angle
    get() = Angle(toDouble() * DEG_TO_RAD)

data class Angle(val rad: Double){
    val cos:Double get() = cos(rad)
    val sin:Double get() = sin(rad)
    val tan:Double get() = tan(rad)
    val deg:Double get() = rad * RAD_TO_DEG

    fun normalize():Angle =
            if (rad >= 0) Angle(rad % TAU_ANGLE.rad)
            else Angle((rad % TAU_ANGLE.rad) + TAU_ANGLE.rad)

    operator fun plus(angle: Angle)     = Angle(rad + angle.rad)
    operator fun minus(angle: Angle)    = Angle(rad - angle.rad)
    operator fun times(d: Number)       = Angle(rad * d.toDouble())
    operator fun div(d: Number)         = Angle(rad / d.toDouble())
    operator fun div(other: Angle)      = rad / other.rad

}

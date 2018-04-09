package com.example.water.cproject.genuino

/**
 * Created by water on 2017-04-18.
 */

class IMU {
    var x: Float = 0.toFloat()
        private set
    var y: Float = 0.toFloat()
        private set
    var z: Float = 0.toFloat()
        private set
    val imu: IMU
        get() = this

    init {
        updateAngle(0f, 0f, 0f)
    }

    fun updateAngle(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }
}

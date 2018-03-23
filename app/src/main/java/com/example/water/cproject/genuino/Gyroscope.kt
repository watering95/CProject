package com.example.water.cproject.genuino

/**
 * Created by water on 2017-04-18.
 */

class Gyroscope {
    var x: Float = 0.toFloat()
        private set
    var y: Float = 0.toFloat()
        private set
    var z: Float = 0.toFloat()
        private set
    val data: Gyroscope
        get() = this

    init {
        updateData(0f, 0f, 0f)
    }

    fun updateData(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }
}

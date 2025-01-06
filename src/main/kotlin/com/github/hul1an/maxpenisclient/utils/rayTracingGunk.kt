package com.github.hul1an.maxpenisclient.utils

class RayTraceUtils {

    var sides: Array<DoubleArray> = arrayOf(
        doubleArrayOf(0.01, 0.5, 0.5),
        doubleArrayOf(0.99, 0.5, 0.5),
        doubleArrayOf(0.5, 0.5, 0.01),
        doubleArrayOf(0.5, 0.5, 0.99),
        doubleArrayOf(0.5, 0.04, 0.5),
        doubleArrayOf(0.5, 0.96, 0.5)
    )


    fun updateSides(sides: Array<DoubleArray>) {
        this.sides = sides
    }


}
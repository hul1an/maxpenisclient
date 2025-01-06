package com.github.hul1an.maxpenisclient.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Waypoint(val name: String, val x: Double, val y: Double, val z: Double)

@Serializable
data class Area(val name: String, val waypoints: MutableList<Waypoint>)



class RouteWalker {

    enum class MacroStates {
        WALKING,
        WAITING
    }

    private val areas: MutableList<Area> = mutableListOf()


    private var rotations: Boolean
    private var loadingCords: Boolean
    private var stopOnEnd: Boolean
    private var rotationTime: Int
    private var callBackActions: Array<Any>
    private var state: MacroStates
    private var currentIndexWalk: Int
    private var currentIndexLook: Int
    private var path: Array<Any>
    private var Enabled: Boolean

    init {
        this.Enabled = false
        this.path = emptyArray()
        this.currentIndexWalk = 0
        this.currentIndexLook = 0
        this.state = MacroStates.WAITING
        this.callBackActions = emptyArray()
        this.rotationTime = 200
        this.stopOnEnd = false
        this.loadingCords = false
        this.rotations = true

    }






    fun addWaypoint(areaName: String, waypoint: Waypoint) {
        val area = areas.find { it.name == areaName } ?: Area(areaName, mutableListOf()).also { areas.add(it) }
        area.waypoints.add(waypoint)
        println("Waypoint added: $waypoint")
        saveAreasToFile()
    }
    fun removeWaypoint(areaName: String, waypointName: String): Boolean {
        val area = areas.find { it.name == areaName }
        return if (area != null) {
            val waypoint = area.waypoints.find { it.name == waypointName }
            if (waypoint != null) {
                area.waypoints.remove(waypoint)
                println("Waypoint removed: $waypoint")
                saveAreasToFile()
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun saveAreasToFile() {
        val jsonString = Json.encodeToString(areas)
        val filePath = "D:/maxpenisclient/src/main/kotlin/com/github/hul1an/maxpenisclient/utils/locations.json"
        println("Saving areas to file: $jsonString")
        println("File path: $filePath")
        File(filePath).writeText(jsonString)
    }


}






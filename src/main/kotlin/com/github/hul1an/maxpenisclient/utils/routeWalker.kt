package com.github.hul1an.maxpenisclient.utils

import com.github.hul1an.maxpenisclient.MyConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.common.MinecraftForge
import java.io.File
import java.util.concurrent.Executors

val scheduler = Executors.newScheduledThreadPool(1)

@Serializable
data class Waypoint(val name: String, val x: Double, val y: Double, val z: Double)

@Serializable
data class Area(val name: String, val waypoints: MutableList<Waypoint>)

class RouteWalker {

    enum class MacroStates {
        WALKING,
        WAITING
    }

    private var rotate: Boolean = false
    private val areas: MutableList<Area> = mutableListOf()

    val MathUtils = MathUtilsClass()
    val config = MyConfig()
    val movementHelper = MovementHelper()
    val Rotations = Rotations()

    private var rotations: Boolean
    private var loadingCords: Boolean
    private var stopOnEnd: Boolean
    private var rotationTime: Int
    private var callBackActions: Array<() -> Unit>
    private var state: MacroStates
    private var currentIndexWalk: Int
    private var currentIndexLook: Int
    private var path: Array<Array<Double>>
    private var Enabled: Boolean

    init {
        this.Enabled = false
        this.path = emptyArray()
        this.currentIndexWalk = 0
        this.currentIndexLook = 0
        this.state = MacroStates.WAITING
        this.callBackActions = emptyArray()
        this.rotationTime = 300
        this.stopOnEnd = false
        this.loadingCords = false
        this.rotations = true

        // Register to the event bus
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (this.Enabled) {
            if (this.currentIndexWalk == this.path.size) {
                //println("Reached end of path, stopping")
                this.currentIndexWalk = 0
                this.currentIndexLook = 2
                this.triggerEnd()
                this.Enabled = false
                this.state = MacroStates.WAITING
                Rotations.stopRotate()
                movementHelper.stopMovement()
                return
            }
            if (this.state == MacroStates.WALKING) {
                if (Minecraft.getMinecraft().currentScreen != null) {
                    movementHelper.stopMovement()
                    return
                }
                if (this.path.isNotEmpty()) {
                    if (this.currentIndexLook >= this.path.size) {
                        this.currentIndexLook = this.path.size - 1
                    }
                    var currentWalk = this.path[this.currentIndexWalk]
                    var currentLook = this.path[this.currentIndexLook]
                    val distancePoint = MathUtils.distanceToPlayer(arrayOf(currentWalk[0] + 0.5, currentWalk[1] + 1.52, currentWalk[2] + 0.5))
                    if (distancePoint["distance"]!! < 6.0 && distancePoint["distanceFlat"]!! < 0.8) {
                        //println("Reached point $currentWalk, moving to next point")
                        this.currentIndexWalk += 1
                        this.currentIndexLook += 1
                        if (this.currentIndexWalk == this.path.size) {
                            println("Reached end of path, stopping")
                            this.currentIndexWalk = 0
                            this.currentIndexLook = 2
                            this.triggerEnd()
                            this.Enabled = false
                            this.state = MacroStates.WAITING
                            Rotations.stopRotate()
                            movementHelper.stopMovement()
                            return
                        }
                        if (this.currentIndexLook >= this.path.size) {
                            this.currentIndexLook = this.path.size - 1
                        }
                        currentWalk = this.path[this.currentIndexWalk]
                        currentLook = this.path[this.currentIndexLook]
                    }

                    try {
                        val vec3PointLook = Vec3(currentLook[0] + 0.5, currentLook[1] + 1.52, currentLook[2] + 0.5)
                        val vec3PointWalk = Vec3(currentWalk[0] + 0.5, currentWalk[1] + 1.0, currentWalk[2] + 0.5)
                        val angles = MathUtils.calculateAngles(vec3PointWalk)
                        movementHelper.setKeysBasedOnYaw(angles.first) // first is yaw
                        if (this.rotations) {
                            //println("Rotating to $vec3PointLook")
                            Rotations.rotateTo(vec3PointLook)
                        } else {
                            println("Rotations are disabled")
                        }
                    } catch (error: Exception) {
                        println("Error during movement: ${error.message}")
                        error.printStackTrace()
                    }
                }
            }
        }
    }

    fun toggle() {
        this.Enabled = !this.Enabled
        //println("RouteWalker toggled, Enabled: $Enabled")
        if (this.Enabled) {
            this.state = MacroStates.WALKING
            val index = this.getClosestIndex()
            this.currentIndexLook = index
            this.currentIndexWalk = index
            //println("RouteWalker enabled, starting at index $index")
        } else {
            this.state = MacroStates.WAITING
            Rotations.stopRotate()
            movementHelper.stopMovement()
            //println("RouteWalker disabled")
        }
    }

    fun setPath(path: Array<Array<Double>>?) {
        this.currentIndexWalk = 0
        this.currentIndexLook = 2
        if (path == null) {
            this.path = emptyArray()
        } else {
            if (this.currentIndexLook >= path.size) {
                this.currentIndexLook = path.size - 1
            }
            this.path = path
        }
        //println("Path set with ${this.path.size} points")
    }

    fun getClosestIndex(): Int {
        var closest: Any? = null
        var closestIndex = 0
        var closestDistance = Double.MAX_VALUE

        this.path.forEachIndexed { index, point ->
            val distance = MathUtils.distanceToPlayer(point)["distance"] ?: Double.MAX_VALUE
            if (closest == null || distance < closestDistance) {
                closest = point
                closestIndex = index
                closestDistance = distance
            }
        }
        //println("Closest index to player: $closestIndex")
        return closestIndex
    }

    fun setRotate(rotate: Boolean) {
        this.rotate = rotate
    }

    fun setRotationTime(time: Int) {
        this.rotationTime = time
    }

    fun setStopOnEnd(stop: Boolean) {
        this.stopOnEnd = stop
    }

    fun setRotations(rotations: Boolean) {
        this.rotations = rotations
    }

    fun triggerOnEnd(callBack: () -> Unit) {
        this.callBackActions = this.callBackActions + callBack
    }

    fun triggerEnd() {
        this.callBackActions.forEach { action ->
            action()
        }
        this.callBackActions = emptyArray()
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

    fun loadPathFromJson(areaName: String): Array<Array<Double>>? {
        val filePath = "D:/maxpenisclient/src/main/kotlin/com/github/hul1an/maxpenisclient/utils/locations.json"
        val jsonString = File(filePath).readText()
        val areas: List<Area> = Json.decodeFromString(jsonString)

        val area = areas.find { it.name == areaName } ?: return null
        return area.waypoints.map { waypoint ->
            arrayOf(waypoint.x, waypoint.y, waypoint.z)
        }.toTypedArray()
    }
}
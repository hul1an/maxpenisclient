package com.github.hul1an.maxpenisclient.features

import com.github.hul1an.maxpenisclient.MyConfig
import com.github.hul1an.maxpenisclient.utils.*
import com.github.hul1an.maxpenisclient.utils.RouteWalker.MacroStates
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.Timer
import java.util.TimerTask

class HighliteMacro {

    enum class MacroStates {
        WALKING,
        WAITING,
        MINING,
        RETURNING,
        WARPING
    }
    enum class MacroActions {
        WALKING,
        WAITING,
        MINING,
        RETURNING
    }



    val config = MyConfig()
    val movementHelper = MovementHelper()
    val rotations = Rotations()
    val location = LocationUtils
    val routeWalker = RouteWalker()
    val mathUtils = MathUtilsClass()

    private var finalAge: Int = 3
    private var Enabled: Boolean
    private var moving2Mountaintop: Boolean = false
    private var whitelist: Array<Any>
    private var sides: Array<DoubleArray> = arrayOf(
        doubleArrayOf(0.5, 0.01, 0.5),
        doubleArrayOf(0.5, 0.98, 0.5),
        doubleArrayOf(0.01, 0.5, 0.5),
        doubleArrayOf(0.98, 0.5, 0.5),
        doubleArrayOf(0.5, 0.5, 0.01),
        doubleArrayOf(0.5, 0.5, 0.99)
    )
    private var state: MacroStates
    private var action: MacroActions

    private var returnTimer = TimeHelper()
    private var menuCooldown = TimeHelper()
    private var interactWithNPCTimer = TimeHelper()


    var riftCollapse = false
    var infused = false
    //var warpedToWizard = false



    init { //equivalent to constructor in js
        this.Enabled = false
        this.whitelist = emptyArray()
        //toggle works on keybind and is called from ModCore.kt :)
        when (config.finalAge) {
            0 -> this.finalAge = 3 //youngite
            1 -> this.finalAge = 11 //timeite
            2 -> this.finalAge = 10 //obsolite
        }

        this.state = MacroStates.WAITING
        this.action = MacroActions.WAITING

        MinecraftForge.EVENT_BUS.register((this))




    }


    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            if (!this.Enabled) {
                return
            }

            if (Minecraft.getMinecraft().currentScreen != null) {
                //println("Current screen is not null, stopping movement")
                movementHelper.stopMovement()
                movementHelper.setKey("shift", false)
                movementHelper.setKey("leftclick", false)
                movementHelper.setKey("sprint", false)
                rotations.stopRotate()

                if (location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 1.0) && this.state == MacroStates.RETURNING){
                    interactWithNPCTimer.reset()
                    val currentScreen = Minecraft.getMinecraft().currentScreen
                    if (currentScreen is GuiChest) {
                        val currentScreen1 = currentScreen as GuiChest
                        val container = currentScreen1.inventorySlots as ContainerChest
                        for (i in 0 until container.lowerChestInventory.sizeInventory) {
                            val stack = container.lowerChestInventory.getStackInSlot(i)
                            if (stack != null) {
                                if (stack.item == Item.getItemFromBlock(Blocks.double_plant) && stack.metadata == 1) {
                                    if(menuCooldown.hasReached(1000)) {

                                       Minecraft.getMinecraft().playerController.windowClick(
                                           container.windowId, i, 2, 3, Minecraft.getMinecraft().thePlayer
                                       )
                                        menuCooldown.reset()
                                        returnTimer.reset()
                                        infused = true
                                        println("clicked that yordan")
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                return
            }



            if (this.Enabled) {

                //handles returning to mountaintop
                if(this.state == MacroStates.RETURNING){
                    if(isPlayerNearCoordinates(42.5, 122.0, 69.0, 2.0) && location.currentArea == Island.Hub){ ///warp wizard coordinates
                        val path = routeWalker.loadPathFromJson("wizardToRift1")
                        if (path != null) {
                            if(this.returnTimer.hasReached(2000)) {
                                routeWalker.setPath(path)
                                routeWalker.toggle()


                                interactWithNPCTimer.reset() //premptively reset cooldown so wizard menu doesnt insta open or some shit idk
                            }
                        }
                        return
                    }
                    else if (!isPlayerNearCoordinates(42.5, 122.0, 69.0, 10.0) && location.currentArea != Island.TheRift && location.currentArea != Island.Unknown){
                        if(this.returnTimer.hasReached(5000)) {
                            println("time 2 warp")
                            sendChatMessage("/warp wizard")
                            this.returnTimer.reset()
                        }
                    }
                    if (location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 1.0)) {
                        if(interactWithNPCTimer.hasReached(4000)) {
                            println("time2talk2wizzy")
                            val currentScreen = Minecraft.getMinecraft().currentScreen
                            if (currentScreen == null) {
                                if(this.interactWithNpc(46.5, 122.0, 75.5)) {
                                    println("interacted with that yordan")
                                    this.menuCooldown.reset()
                                    this.interactWithNPCTimer.reset()
                                }
                            }
                        }

                    }
                    if (infused && location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 1.0)) {
                        val path = routeWalker.loadPathFromJson("wizardToRift2")
                        if (path != null) {
                            //if(this.returnTimer.hasReached(1000)) {
                                routeWalker.setPath(path)
                                routeWalker.toggle()


                            //}
                        }
                    }
                    if (location.currentArea == Island.TheRift && this.state == MacroStates.RETURNING) {
                        infused = false

                        if(isPlayerNearCoordinates(-44.3, 122.0, 69.3, 1.0)) {
                            val path = routeWalker.loadPathFromJson("riftToEye1")
                        }

                    }
                }
            }

        }




    }






/*
                if (location.currentArea.isArea(Island.TheRift) && routeWalker.getClosestIndex() > 0 && testFuck) {
                    println("Starting RouteWalker on the current route")
                    routeWalker.toggle()
                } else if (location.currentArea.isArea(Island.Hub) /*|| riftCollapse*/ && testFuck) {
                    Thread.sleep(300)
                    if(!warpedToWizard) {
                        println("Warping to wizard")
                        sendChatMessage("/warp wizard")
                        var warpedToWizard = true
                        if (riftCollapse) {
                            riftCollapse = false
                        }
                    }
                    val path = routeWalker.loadPathFromJson("wizardToRift1")
                    if (path != null) {
                        routeWalker.setPath(path)
                        Thread.sleep(300)
                        routeWalker.toggle()
                    }
                } else if (location.currentArea == Island.Hub && routeWalker.getClosestIndex() == 3 && isPlayerNearCoordinates(46.5, 122.0, 75.5, 3.0)) {
                    println("Interacting with NPC")
                    interactWithNpc(46.49, 122.0, 75.52)
                    val currentScreen = Minecraft.getMinecraft().currentScreen
                    if (currentScreen == null || currentScreen !is GuiChest) {
                        println("Current screen is not a GuiChest")
                        return
                    } else if (currentScreen is GuiChest) {
                        println("Current screen is a GuiChest")
                        val currentScreen1 = currentScreen as GuiChest
                        val container = currentScreen1.inventorySlots as ContainerChest
                        for (i in 0 until container.lowerChestInventory.sizeInventory) {
                            val stack = container.lowerChestInventory.getStackInSlot(i)
                            if (stack != null) {
                                val lore = stack.tagCompound?.getTagList("display", 10)?.getCompoundTagAt(0)?.getTagList("Lore", 8)
                                if (lore != null) {
                                    for (j in 0 until lore.tagCount()) {
                                        val loreComponent = lore.getStringTagAt(j)
                                        if (loreComponent.contains("Dimensional Infusion")) {
                                            println("Found Dimensional Infusion, clicking the window")
                                            Thread.sleep(300)
                                            Minecraft.getMinecraft().playerController.windowClick(
                                                container.windowId, i, 2, 3, Minecraft.getMinecraft().thePlayer
                                            )
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(-44.3, 122.0, 69.3, 2.0)) {
                    println("Starting RouteWalker on riftToEye1")
                    val path = routeWalker.loadPathFromJson("riftToEye1")
                    if (path != null) {
                        routeWalker.setPath(path)
                        routeWalker.toggle()
                        warpedToWizard=false
                    }
                    if (path != null && isPlayerNearCoordinates(-43.5, 110.5, 72.5, 2.0)) {
                        routeWalker.setPath(routeWalker.loadPathFromJson("riftToEye2"))
                        routeWalker.toggle()
                    }
                } else if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(-51.5, 104.0, 73.5, 2.0)) {
                    if (isPlayerNearCoordinates(-52.0, 104.0, 70.0, 4.5)) {
                        println("Interacting with eye")
                        //interact with eye...
                    }
                } else if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(48.5, 169.0, 38.5, 2.0)) {
                    println("Starting RouteWalker on eyeToMine")
                    val path = routeWalker.loadPathFromJson("eyeToMine")
                    if (path != null) {
                        routeWalker.setPath(path)
                        routeWalker.toggle()
                    }
                } else if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(18.5, 122.0, 38.5, 2.0)) {
                    println("Starting RouteWalker on miningWalkerRoute")
                    val path = routeWalker.loadPathFromJson("miningWalkerRoute")
                    if (path != null) {
                        routeWalker.setPath(path)
                        routeWalker.toggle()
                    }
                }
            }
        }
    }*/

    fun checkPlayerCurrentIsland() { //debug
        val currentIsland = location.currentArea
        println("The player is currently on: $currentIsland")
    }


    fun interactWithNpc(x: Double, y: Double, z: Double): Boolean { //returns boolean use in if statement
        val players = Minecraft.getMinecraft().theWorld.playerEntities
        var found = false
        for (player in players) {
            if (mathUtils.calculateDistance(arrayOf(player.posX, player.posY, player.posZ), arrayOf(x, y, z))["distanceFlat"]!! < 0.001) {
                found = true
                rotations.rotateTo(Vec3(player.posX, player.posY + player.eyeHeight - 0.4, player.posZ), 5.0f)
                rotations.onEndRotation {
                    Minecraft.getMinecraft().theWorld.playerEntities.forEach { p ->
                        if (p.posX == x && p.posY == y && p.posZ == z && mathUtils.distanceToPlayerCT(p)["distance"]!! < 5) {
                            Minecraft.getMinecraft().playerController.interactWithEntitySendPacket(Minecraft.getMinecraft().thePlayer, p)
                        }
                    }
                }
                break
            }
        }
        return found
    }

    fun isPlayerNearCoordinates(x: Double, y: Double, z: Double, threshold: Double): Boolean {
        val player = Minecraft.getMinecraft().thePlayer
        val playerPos = arrayOf(player.posX, player.posY, player.posZ)
        val targetPos = arrayOf(x, y, z)
        val distance = mathUtils.calculateDistance(playerPos, targetPos)["distance"]!!
        return distance <= threshold
    }

        @SubscribeEvent
        fun onCollapse(event: ClientChatReceivedEvent) {
            if (event.message.unformattedText.contains("Rift is Collapsing!"))
                riftCollapse = true
        }

        fun toggle() { //called via keybind in config i think (it works)
            System.out.println("toggled")
            this.Enabled = !this.Enabled

            if (this.Enabled) {
                System.out.println("currently enabled")
                //this.startTime = Date.now()
                this.state = MacroStates.WAITING;

                returnTimer.reset()
                menuCooldown.reset()
                interactWithNPCTimer.reset()

                if(location.currentArea != Island.TheRift) {
                    this.state = MacroStates.RETURNING
                    println("state set to returning")
                }
            }
        }

        fun stopBot() {
            this.Enabled = false
            movementHelper.stopMovement()
            movementHelper.setKey("shift", down = false)
            movementHelper.setKey("leftclick", down = false)
            rotations.stopRotate()
        }

        fun sendChatMessage(message: String) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(message)
        }
    }

    class MiningBlock(val blockid: Int, val metadata: Int) {

        fun equals(block: Block): Boolean {
            val blockState = block.defaultState
            return block.getMetaFromState(blockState) == this.metadata && Block.getIdFromBlock(block) == this.blockid
        }

        fun equalsNumbers(blockid: Int, metadata: Int): Boolean {
            return blockid == this.blockid && metadata == this.metadata
        }
    }

    class MineTarget(val pos: BlockPos, val point: Vec3, block: Block) {
        val metadata: Int = block.getMetaFromState(block.defaultState)
        val blockid: Int = Block.getIdFromBlock(block)
    }

class MineVein(val positions: Array<BlockPos>)
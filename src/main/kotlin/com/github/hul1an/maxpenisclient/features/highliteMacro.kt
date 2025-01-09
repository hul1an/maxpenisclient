package com.github.hul1an.maxpenisclient.features

import com.github.hul1an.maxpenisclient.MyConfig
import com.github.hul1an.maxpenisclient.utils.*
import com.github.hul1an.maxpenisclient.utils.RouteWalker.MacroStates
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
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
        CRAFTING
    }
    enum class MacroActions { //unused currently
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
    val blockScan = BlockScanClass()

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
    private var Youngite: String = "All sparkle and no substance"
    private var Timeite: String = "Older, wiser, but refusing to admit"
    private var Obsolite: String = "Practically a fossil"
    private var state: MacroStates
    private var action: MacroActions
    private var curMining: Boolean = false

    private var returnTimer = TimeHelper()
    private var menuCooldown = TimeHelper()
    private var interactWithNPCTimer = TimeHelper()

    var miningTestSleep = TimeHelper()


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
                rotations.stopRotate()

                if (location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 3.0) && this.state == MacroStates.RETURNING){ //handles scanning for and buying rift infusion
                    this.interactWithNPCTimer.reset()
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
                                        this.menuCooldown.reset()
                                        this.returnTimer.reset()
                                        println("clicked that yordan")
                                        infused = true
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
                if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(-50.5, 104.0, 70.5, 3.0)&& this.state == MacroStates.RETURNING) {
                    this.interactWithNPCTimer.reset()
                    val currentScreen = Minecraft.getMinecraft().currentScreen
                    if (currentScreen is GuiChest) {
                        val currentScreen1 = currentScreen as GuiChest
                        val container = currentScreen1.inventorySlots as ContainerChest
                        for (i in 0 until container.lowerChestInventory.sizeInventory) {
                            val stack = container.lowerChestInventory.getStackInSlot(i)
                            if (stack != null && stack.hasTagCompound()) {
                                val lore = stack.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)
                                if (lore != null) {
                                    for (j in 0 until lore.tagCount()) {
                                        val loreLine = lore.getStringTagAt(j)
                                        if (loreLine.contains("§7Location: §fMountaintop")) {
                                            if (menuCooldown.hasReached(1000)) {
                                                println("Menu cooldown reached, attempting to click")
                                                Minecraft.getMinecraft().playerController.windowClick(
                                                    container.windowId, i, 0, 0, Minecraft.getMinecraft().thePlayer
                                                )
                                                this.menuCooldown.reset()
                                                this.returnTimer.reset()
                                                println("Clicked that yordan at slot $i")
                                                infused = true
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return
            }


            // MAIN MINING STUFF HERE
            if (this.Enabled) {
              //  if (location.isInSkyblock == false) {
                 //   println("not in skyblock nigger")
                 //   return
              //  }

                //handles returning to mountaintop
                if(this.state == MacroStates.RETURNING){
                    if(isPlayerNearCoordinates(42.5, 122.0, 69.0, 2.0) && location.currentArea == Island.Hub){ ///warp wizard coordinates
                        val path = routeWalker.loadPathFromJson("wizardToRift1")
                        if (path != null) {
                            if(this.returnTimer.hasReached(2000)) {
                                routeWalker.setPath(path)
                                routeWalker.toggle()
                                this.returnTimer.reset()
                                this.interactWithNPCTimer.reset() //premptively reset cooldown so wizard menu doesnt insta open or some shit idk
                            }
                        }
                        return
                    }
                    else if (!isPlayerNearCoordinates(42.5, 122.0, 69.0, 10.0) && location.currentArea != Island.TheRift && location.currentArea != Island.Unknown){ // warps 2 wizard
                        if(this.returnTimer.hasReached(3000)) {
                            println("time 2 warp")
                            sendChatMessage("/warp wizard")
                            this.returnTimer.reset()
                        }
                    }
                    if (location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 3.0)) {
                        if(interactWithNPCTimer.hasReached(2000)) {
                            println("time2talk2wizzy")
                            val currentScreen = Minecraft.getMinecraft().currentScreen
                            if (currentScreen == null) {
                                if(this.interactWithNpc(46.5, 122.0, 75.5)) {
                                    println("interacted with that yordan")
                                    this.menuCooldown.reset()
                                    this.interactWithNPCTimer.reset()
                                    this.returnTimer.reset()
                                }
                            }
                        }

                    }
                    if (infused && location.currentArea == Island.Hub && isPlayerNearCoordinates(46.5, 122.0, 75.5, 2.0)) {
                        val path = routeWalker.loadPathFromJson("wizardToRift2")
                        if (path != null) {
                            if(this.returnTimer.hasReached(1000)) {
                                routeWalker.setPath(path)
                                routeWalker.toggle()
                                infused = false
                                this.returnTimer.reset()

                            }
                        }
                    }
                    if (location.currentArea == Island.TheRift && this.state == MacroStates.RETURNING) {
                        infused = false
                        if(isPlayerNearCoordinates(-44.3, 122.0, 69.3, 2.0)) {
                            if (this.returnTimer.hasReached(2000)) {
                                val path = routeWalker.loadPathFromJson("riftToEye1")
                                routeWalker.setPath(path)
                                routeWalker.toggle()
                                this.returnTimer.reset()

                            }
                        }
                        if(isPlayerNearCoordinates(-43.5, 110.5, 72.5, 2.0)) {
                            if (this.returnTimer.hasReached(3000)) {
                                val path = routeWalker.loadPathFromJson("riftToEye2")
                                routeWalker.setPath(path)
                                routeWalker.toggle()
                                this.returnTimer.reset()

                                this.interactWithNPCTimer.reset() //preemptive reset

                            }
                        }
                        if (location.currentArea == Island.TheRift && isPlayerNearCoordinates(-50.5, 104.0, 70.0, 3.0)) {
                            if (this.interactWithNPCTimer.hasReached(3000)) {
                                println("talk2uheye")
                                val currentScreen = Minecraft.getMinecraft().currentScreen
                                if (currentScreen == null) {
                                    if (this.interactWithEye(-50.5, 104.0, 70.5)) {
                                        println("interacted with that yordan")
                                        this.menuCooldown.reset()
                                        this.interactWithNPCTimer.reset()
                                        this.returnTimer.reset()
                                    }
                                }
                            }
                        }
                        if(isPlayerNearCoordinates(47.5, 169.0, 38.5, 5.0)) {
                            if (this.returnTimer.hasReached(2000)) {
                                val path = routeWalker.loadPathFromJson("eyeToMine")
                                routeWalker.setPath(path)
                                routeWalker.toggle()
                                this.returnTimer.reset()
                                routeWalker.triggerOnEnd {
                                    println("path ended, executing callback")
                                    this.state = MacroStates.MINING
                                    println("state set to ${this.state}")
                                }
                            }
                        }



                    }
                }
                if (this.state == MacroStates.MINING /*&& location.currentArea == Island.TheRift*/) {



                    //checking if we can craft 2 highlite then setting state to crafting
                    val youngiteCount = scanInventory(Youngite)
                    val timeiteCount = scanInventory(Timeite)
                    val obsoliteCount = scanInventory(Obsolite)
                    when {
                        youngiteCount >= 64 && timeiteCount >= 64 && obsoliteCount >= 32 -> {
                            this.menuCooldown.reset()
                            this.state = MacroStates.CRAFTING
                            this.finalAge = 3 // set back to youngite
                        }
                        obsoliteCount >= 32 && timeiteCount >= 64 -> {
                            this.finalAge = if (youngiteCount < 64) 3 else this.finalAge
                        }
                        timeiteCount >= 64 -> {
                            this.finalAge = 10
                        }
                        youngiteCount >= 64 -> {
                            this.finalAge = 11
                        }
                    }

                    //mining logic here
                    if (blockScan.fullSortScan().size >= 1) {
                        val world = Minecraft.getMinecraft().theWorld
                        var currentBlock = blockScan.fullSortScan()[0]
                        var currentBlockPos = BlockPos(currentBlock)
                        var currentBlockState = world.getBlockState(currentBlockPos)
                        var currentBlockBlock = currentBlockState.block
                        var currentBlockMeta = currentBlockBlock.getMetaFromState(currentBlockState)

                        if (currentBlockMeta == this.finalAge) {
                            if (mineBlock(currentBlock)) {
                                if (blockScan.fullSortScan().size == 0) {
                                    movementHelper.setKey("leftclick", down = false)
                                    this.state = MacroStates.WALKING
                                }
                            }
                        }
                        else if(currentBlockMeta != this.finalAge) {
                            //age
                        }
                    }
                }


                if (this.state == MacroStates.WALKING && location.currentArea == Island.TheRift){

                    val path = routeWalker.loadPathFromJson("miningRoute")
                    if(path != null) { //sets route walker to the mining path and toggles on
                        routeWalker.setPath(path)
                        routeWalker.toggle()

                        val closestCoordinates = routeWalker.getClosestCoordinates()
                        if (closestCoordinates != null && closestCoordinates.size == 3) {
                            val (x, y, z) = closestCoordinates
                            if (isPlayerNearCoordinates(x, y, z, 2.0)) {
                                if(blockScan.fullSortScan().size >= 1) {
                                    println("found blocks to mine")
                                    this.state = MacroStates.MINING
                                }
                            }
                        }
                    }
                }
                if (this.state == MacroStates.CRAFTING) {
                    if(this.menuCooldown.hasReached(500) && Minecraft.getMinecraft().currentScreen == null) {
                        sendChatMessage("/craft")
                        println("opening craft vro")
                        this.menuCooldown.reset()
                    }
                    if(Minecraft.getMinecraft().currentScreen != null) {
                        return
                    }




                    //crafting logic goes here vro


                    //craft highlite
                    //set state to walking

                    this.state = MacroStates.WALKING
                }
            }

        }




    }




    fun checkPlayerCurrentIsland() { //debug
        val currentIsland = location.currentArea
        println("The player is currently on: $currentIsland")
    }

    fun scanInventory(itemLore: String): Int {
        val player = Minecraft.getMinecraft().thePlayer
        val inventory = player.inventory.mainInventory
        var count = 0

        for (itemStack in inventory) {
            if (itemStack != null && itemStack.hasTagCompound()) {
                val lore = itemStack.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)
                if (lore != null) {
                    for (i in 0 until lore.tagCount()) {
                        val loreLine = lore.getStringTagAt(i)
                        if (loreLine.contains(itemLore)) {
                            count += itemStack.stackSize
                            break
                        }
                    }
                }
            }
        }
        return count
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
        this.interactWithNPCTimer.reset()
        return found
    }
    fun interactWithEye(x: Double, y: Double, z: Double): Boolean {
        val zombies = Minecraft.getMinecraft().theWorld.loadedEntityList.filterIsInstance<EntityZombie>()
        var found = false
        for (zombie in zombies) {
            val distance = mathUtils.calculateDistance(arrayOf(zombie.posX, zombie.posY, zombie.posZ), arrayOf(x, y, z))["distance"]!!
            println("Checking zombie at (${zombie.posX}, ${zombie.posY}, ${zombie.posZ}) with distance $distance")
            if (distance <= 4.5) {
                found = true
                rotations.rotateTo(Vec3(zombie.posX, zombie.posY + zombie.eyeHeight - 0.4, zombie.posZ), 5.0f)
                rotations.onEndRotation {
                    Minecraft.getMinecraft().theWorld.loadedEntityList.forEach { entity ->
                        if (entity is EntityZombie) {
                            val entityDistance = mathUtils.distanceToPlayerCT(entity)["distance"]!!
                           // println("Entity at (${entity.posX}, ${entity.posY}, ${entity.posZ}) with distance $entityDistance")
                            if (entityDistance < 4.5) {
                                Minecraft.getMinecraft().playerController.interactWithEntitySendPacket(Minecraft.getMinecraft().thePlayer, entity)
                                println("Sent interact packet to entity at (${entity.posX}, ${entity.posY}, ${entity.posZ})")
                            }
                        }
                    }
                }
                break
            }
        }
        this.interactWithNPCTimer.reset()
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
        if (event.message.unformattedText.contains("Rift is Collapsing!")) {
            riftCollapse = true
        }
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
        if (!this.Enabled) {
            println("bot stopped")
            stopBot()
        }
    }

    fun toggle2() {
        System.out.println("toggled2")
        this.Enabled = !this.Enabled

        if (this.Enabled) {
            println("toggle2 enabled")
            this.state = MacroStates.MINING
        }
        if (!this.Enabled) {
            println("bot stopped")
            stopBot()
        }
    }

    fun stopBot() {
        this.Enabled = false
        movementHelper.stopMovement()
        movementHelper.setKey("shift", down = false)
        movementHelper.setKey("leftclick", down = false)
        routeWalker.triggerEnd()
        rotations.stopRotate()
    }
    fun sendChatMessage(message: String) { Minecraft.getMinecraft().thePlayer.sendChatMessage(message) }


    fun mineBlock(block: BlockPos): Boolean {
        val world = Minecraft.getMinecraft().theWorld
        val playerEyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        val point = rayTraceUtils.getPointOnBlock(block, playerEyes, mcCast = true)

        if (point != null) {
            val blockState = world.getBlockState(block).block



            rotations.rotateTo(Vec3(point[0], point[1], point[2]))
            println("rotating rn")
            rotations.onEndRotation {
                if(!movementHelper.isKeyDown("leftclick")) {
                    movementHelper.setKey("leftclick", down = true)
                    println("left click to true")
                }


            }


        }
        return false
    }
}
    //snap to block
    //start mining
    //wait till block becomes air
    //stop mining
    //:)

    /*
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

     */

//class MineVein(val positions: Array<BlockPos>)
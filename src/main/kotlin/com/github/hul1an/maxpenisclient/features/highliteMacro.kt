package com.github.hul1an.maxpenisclient.features



import com.github.hul1an.maxpenisclient.clock.utils
import com.github.hul1an.maxpenisclient.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent



//Hul1an is solely responsible for this monster of a feature, and only he has the lack of sanity to debug it and work on it
// sigma macro 750 lines of uncommented code :fire: :fire:

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
    private var walkingRoute: Boolean = false

    private var returnTimer = TimeHelper()
    private var menuCooldown = TimeHelper()
    private var interactWithNPCTimer = TimeHelper()
    private var miningRoute1 = routeWalker.loadPathFromJson("miningRoute")
    private val miningRoute2 = routeWalker.loadPathFromJson("miningRouteReturn")
    private var flipflop = 0

    var miningTestSleep = TimeHelper()


    var riftCollapse = false
    var infused = false
    //var warpedToWizard = false



    init { //equivalent to constructor in js
        this.Enabled = false
        this.whitelist = emptyArray()
        //toggle works on keybind and is called from ModCore.kt :)


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
                //handles crafting higlite using quickcraft
                if (location.currentArea == Island.TheRift && this.state == MacroStates.CRAFTING) {
                    utils.customChat("Crafting Highlite")
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
                                        //println(loreLine) debug
                                        if (loreLine.contains("This is what")) {
                                            if (menuCooldown.hasReached(1000)) {
                                                println("Menu cooldown reached, attempting to click")
                                                Minecraft.getMinecraft().playerController.windowClick(
                                                    container.windowId, i, 0, 1, Minecraft.getMinecraft().thePlayer
                                                )
                                                this.menuCooldown.reset()
                                                this.returnTimer.reset()
                                                println("Clicked that yordan at slot $i")
                                                Minecraft.getMinecraft().thePlayer.closeScreen()
                                                this.state = MacroStates.WALKING
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
                if (!location.isInSkyblock) {
                    println("not in skyblock nigger")
                    if (isPlayerNearCoordinates(12.0, 75.0, 2.0, 8.0)){
                        if (this.returnTimer.hasReached(10000))
                        sendChatMessage("/skyblock")
                        this.returnTimer.reset()
                    }

                   return
               }

                if(location.currentArea != Island.TheRift && this.state != MacroStates.RETURNING){
                    this.state = MacroStates.RETURNING
                }
                //TODO
                //currently bugs out on rift collapse and requires user intervention, not ideal.

                if(this.state == MacroStates.RETURNING){
                    if(isPlayerNearCoordinates(42.5, 122.0, 69.0, 2.0) && location.currentArea == Island.Hub){ ///warp wizard coordinates
                        val path = routeWalker.loadPathFromJson("wizardToRift1") //walk to wizard
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
                            utils.customChat("Warping to wizard")
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
                            if (this.interactWithNPCTimer.hasReached(2000)) {
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
                if (this.state == MacroStates.MINING && location.currentArea == Island.TheRift) {
                    println(this.state)


                    //checking if we can craft highlite then setting state to crafting
                    val youngiteCount = scanInventory(Youngite)
                    val timeiteCount = scanInventory(Timeite)
                    val obsoliteCount = scanInventory(Obsolite)
                    when {
                        youngiteCount >= 32 && timeiteCount >= 32 && obsoliteCount >= 16 -> {
                            this.menuCooldown.reset()
                            this.state = MacroStates.CRAFTING
                            this.finalAge = 3 // set back to youngite
                        }
                        obsoliteCount >= 16 && timeiteCount >= 32 -> {
                            this.finalAge = if (youngiteCount < 32) 3 else this.finalAge
                        }
                        timeiteCount >= 32 -> {
                            this.finalAge = 10
                        }
                        youngiteCount >= 32 -> {
                            this.finalAge = 11
                        }
                    }

                    //mining logic here
                    if (blockScan.fullSortScan().size >= 1 ) {
                        val world = Minecraft.getMinecraft().theWorld
                        var currentBlock = blockScan.fullSortScan()[0]
                        var currentBlockPos = BlockPos(currentBlock)
                        var currentBlockState = world.getBlockState(currentBlockPos)
                        var currentBlockBlock = currentBlockState.block
                        var currentBlockMeta = currentBlockBlock.getMetaFromState(currentBlockState)

                        if (currentBlockMeta == this.finalAge) {
                            if (mineBlock(currentBlock)) {
                            }
                        }
                        else if(currentBlockMeta != this.finalAge && this.state == MacroStates.MINING) {
                            movementHelper.setKey("leftclick", down = false) //stops mining
                            if (ageBlock(currentBlock)) {
                                if (blockScan.fullSortScan().size == 0) {
                                    movementHelper.setKey("rightclick", down = false)
                                    this.state = MacroStates.WALKING
                                }
                            }


                        }
                    }
                    if (blockScan.fullSortScan().size == 0 && this.state == MacroStates.MINING) {
                        movementHelper.setKey("leftclick", down = false)
                        movementHelper.setKey("rightclick", down = false)
                        this.state = MacroStates.WALKING
                    }
                }


                if (this.state == MacroStates.WALKING && location.currentArea == Island.TheRift) {

                    if (miningRoute1 != null && !this.walkingRoute) {
                        this.walkingRoute = true
                        routeWalker.setPath(miningRoute1)
                        routeWalker.toggle() // toggles on
                        println("toggled walker")
                        routeWalker.triggerOnEnd {
                            println("route ended le sigma")
                            if (flipflop == 0) {
                                miningRoute1 = routeWalker.loadPathFromJson("miningRouteReturn")
                                flipflop = 1
                            } else if (flipflop == 1) {
                                miningRoute1 = routeWalker.loadPathFromJson("miningRoute")
                                flipflop = 0
                            }
                            this.walkingRoute = false // reset walkingRoute flag
                            routeWalker.setPath(miningRoute1)
                            routeWalker.toggle() // toggles on the new path
                        }
                    }
                    val closestCoordinates = routeWalker.getClosestCoordinates()
                    if (closestCoordinates != null && closestCoordinates.size == 3) { //finds closest/current waypoint coords
                        val (x, y, z) = closestCoordinates
                        if (isPlayerNearCoordinates(x, y, z, 2.0) && !scanForPlayersAroundCoords(x, y, z)) { //if player is near a waypoint and there are no other players
                            if (blockScan.fullSortScan().size >= 1) { //check for blocks
                                println("found blocks to mine")
                                routeWalker.toggle() //stop moving
                                this.state = MacroStates.MINING //set to mining
                                this.walkingRoute = false
                            }
                        }
                    }
                }



                if (this.state == MacroStates.CRAFTING) {
                    println(this.state)
                    if(this.menuCooldown.hasReached(500) && Minecraft.getMinecraft().currentScreen == null) {
                        sendChatMessage("/craft")
                        println("opening craft vro")
                        this.menuCooldown.reset()
                    }
                    val youngiteCount = scanInventory(Youngite)
                    val timeiteCount = scanInventory(Timeite)
                    val obsoliteCount = scanInventory(Obsolite)

                    if (youngiteCount < 32 || timeiteCount < 32 || obsoliteCount < 16) {
                        this.state = MacroStates.MINING
                        println("state set to $state")
                    }

                    println("i should be crafting rn vro")



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
    fun quickCraftFinder(itemLore: String): Int? {
        val player = Minecraft.getMinecraft().thePlayer
        val currentScreen = Minecraft.getMinecraft().currentScreen

        if (currentScreen is GuiChest) {
            val container = currentScreen.inventorySlots as ContainerChest

            for (i in 0 until container.lowerChestInventory.sizeInventory) {
                val itemStack = container.lowerChestInventory.getStackInSlot(i)
                if (itemStack != null && itemStack.hasTagCompound()) {
                    val lore = itemStack.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)
                    if (lore != null) {
                        for (j in 0 until lore.tagCount()) {
                            val loreLine = lore.getStringTagAt(j)
                            if (loreLine.contains(itemLore)) {
                                return i
                            }
                        }
                    }
                }
            }
        }
        return null
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
    fun scanForPlayersAroundCoords(x: Double, y: Double, z: Double): Boolean {
        val players = Minecraft.getMinecraft().theWorld.playerEntities
        val localPlayer = Minecraft.getMinecraft().thePlayer
        var found = false
        for (player in players) {
            if (player != localPlayer && mathUtils.calculateDistance(arrayOf(player.posX, player.posY, player.posZ), arrayOf(x, y, z))["distanceFlat"]!! < 4) {
                found = true
                break
            }
        }
        if (found) {
            return true
        }
        else {
            return false
        }
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
            utils.customChat("highlite macro enabled")
            MouseUtils.unGrabMouse()
            System.out.println("currently enabled")
            //this.startTime = Date.now()
            this.state = MacroStates.WAITING;

            returnTimer.reset()
            menuCooldown.reset()
            interactWithNPCTimer.reset()

            if(location.currentArea != Island.TheRift) {
                this.state = MacroStates.RETURNING
                println("state set to $state")
            }
            if(location.currentArea == Island.TheRift) {
                this.state = MacroStates.WALKING
                println("state set to $state")
            }
        }
        if (!this.Enabled) {
            utils.customChat("highlite macro disabled")

            println("bot stopped")
            stopBot()
        }
    }

    fun toggle2() { //test toggle
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
        this.walkingRoute = false
        movementHelper.stopMovement()
        movementHelper.setKey("shift", down = false)
        movementHelper.setKey("leftclick", down = false)
        movementHelper.setKey("rightclick", down = false)
        val path = routeWalker.loadPathFromJson("sexroute9000") //null/empty path to stop route walker
        routeWalker.setPath(path)
        routeWalker.toggle()
        rotations.stopRotate()
        MouseUtils.reGrabMouse()
    }

    fun sendChatMessage(message: String) { Minecraft.getMinecraft().thePlayer.sendChatMessage(message) }


    fun mineBlock(block: BlockPos): Boolean {
        val world = Minecraft.getMinecraft().theWorld
        val playerEyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        val point = rayTraceUtils.getPointOnBlock(block, playerEyes, mcCast = true)

        if (point != null) {
            val blockState = world.getBlockState(block).block
            //find iron pickaxe in hotbar and set held item

            setHeldItemInHotbar(257)
            rotations.rotateTo(Vec3(point[0], point[1], point[2]))
            rotations.onEndRotation {
                if(!movementHelper.isKeyDown("leftclick")) {
                    movementHelper.setKey("leftclick", down = true)
                    println("left click to true")
                }


            }


        }
        return false
    }

    fun ageBlock(block: BlockPos): Boolean {
        val world = Minecraft.getMinecraft().theWorld
        val playerEyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(1.0f)
        val point = rayTraceUtils.getPointOnBlock(block, playerEyes, mcCast = true)

        if (point != null) {
            val blockState = world.getBlockState(block)
            val blockMeta = blockState.block.getMetaFromState(blockState)

            if (blockMeta != this.finalAge) {
                //find diamond horse armor in hotbar and set as held item
                setHeldItemInHotbar(419)
                rotations.rotateTo(Vec3(point[0], point[1], point[2]))
                rotations.onEndRotation {
                    if(!movementHelper.isKeyDown("rightclick")) {
                        movementHelper.setKey("rightclick", down = true)
                        println("right click to true")
                    }
                }
            }
        }
        return false
    }

    fun setHeldItemInHotbar(itemId: Int) {
        val player = Minecraft.getMinecraft().thePlayer
        val inventory = player.inventory.mainInventory

        // Check if the player is already holding the item
        val currentItem = player.inventory.getCurrentItem()
        if (currentItem != null && Item.getIdFromItem(currentItem.item) == itemId) {
            return
        }

        // Check if an item with the matching item ID is in inventory
        for (i in inventory.indices) {
            val itemStack = inventory[i]
            if (itemStack != null && Item.getIdFromItem(itemStack.item) == itemId) {
                // Check if the item is in hotbar slots 1-8
                if (i in 0..8) {
                    // If it is in hotbar, set held item
                    player.inventory.currentItem = i
                    println("set held item to $i")
                    return
                }
            }
        }
    }


}

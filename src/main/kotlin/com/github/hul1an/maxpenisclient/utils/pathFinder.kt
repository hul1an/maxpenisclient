package com.github.hul1an.maxpenisclient.utils

import cc.polyfrost.oneconfig.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import kotlin.math.*








    //GCost is the distance to the start node
    //HCost is the distance to the end node
    //FCost is the sumation of G and H Cost

    //Make a Pathfinder that can navigate a 3d Enviroment

    class Node(val pos: BlockPos, val dist: Double) {
        val id: Int = pos.hashCode()
        var gCost: Double? = null //Cost to Start Node
        var hCost: Double? = null //Cost to End Node
        var fCost: Double? = null //Total cost
        var parent: Node? = null

        fun setFCost() {
            (gCost!! + hCost!!).also { this.fCost = it } //What the fuck
        }
    }



    class GridWorld {
        var heightCost: Int = 2
        var world = Minecraft.getMinecraft().theWorld

        fun nodeFromWorld(pos: BlockPos): Node {
            return Node(pos, 0.0)
        }

        fun getNeighbors(node:Node ): MutableList<Node> {
            //get neighbors
            val neighbors = mutableListOf<Node>()
            val position = node.pos
            //cardinal directions (((x, y, z)), dist))
            neighbors.add(Node(position.add(Vec3i(1, 0, 0)), 2.0))
            neighbors.add(Node(position.add(Vec3i(-1,0,0)), 2.0 ))
            neighbors.add(Node(position.add(Vec3i(0,0,1)), 2.0 ))
            neighbors.add(Node(position.add(Vec3i(1,0,-1)), 2.0 ))

            //diagonal
           // neighbors.add(Node(position.add(Vec3i(1, 0, 1)), 2.8))
           // neighbors.add(Node(position.add(Vec3i(1, 0, -1)), 2.8))
           // neighbors.add(Node(position.add(Vec3i(-1, 0, 1)), 2.8))
           // neighbors.add(Node(position.add(Vec3i(-1, 0, -1)), 2.8))

            //gonna try without diagonal + height variation, might fuck something up idk

            //cardinal + down
            neighbors.add(Node(position.add(Vec3i(1, -1, 0)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(-1,-1,0)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(0,-1,1)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(1,-1,-1)), heightCost.toDouble()))

            //cardinal + up
            neighbors.add(Node(position.add(Vec3i(1, 1, 0)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(-1,1,0)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(0,1,1)), heightCost.toDouble()))
            neighbors.add(Node(position.add(Vec3i(1,1,-1)), heightCost.toDouble()))

            return neighbors
        }


        fun isAir(pos: BlockPos): Boolean {
            return world.getBlockState(pos).block.isAir(world, pos) //erm might work idk
        }
    }



    class PathFinderClass {
        var Grid = GridWorld()
        var mathUtils = MathUtilsClass()

        init {
            var calculating = false
            var maxCalculationTime = 500
            var smallArea = false
            var heightDifference = true
        }

        fun aStar() {
            //not sigma
        }


    }




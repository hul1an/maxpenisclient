package com.github.hul1an.maxpenisclient

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.KeyBind
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.OptionSize
import cc.polyfrost.oneconfig.libs.universal.UKeyboard


object MyConfig : Config(Mod("maxpenisaddons", ModType.SKYBLOCK), "maxpenisConfig.json") {

    @KeyBind(name = "Highlite Miner Keybind", category = "Rift", subcategory = "mountaintop")
    lateinit var minerKeyBind: OneKeyBind

    init {
        initialize()
        minerKeyBind = OneKeyBind(UKeyboard.KEY_B)

        addDependency("subSwitch") { masterSwitch }
        addDependency("rotationSmoothness") { highliteMinerMainToggle }
        addDependency("minerKeyBind") { highliteMinerMainToggle }

        addListener("rotationSmoothness") { save() }
    }

    @Switch(name = "Master Switch", size = OptionSize.DUAL, category = "General", subcategory = "Switches")
    var masterSwitch: Boolean = false

    @Switch(name = "Sub Switch", size = OptionSize.DUAL, category = "General", subcategory = "Switches")
    var subSwitch: Boolean = false

    @Switch(name = "Highlite Miner", size = OptionSize.DUAL, category = "Rift", subcategory = "mountaintop")
    var highliteMinerMainToggle: Boolean = false

    @Slider(name = "Rotation Smoothness", min = 5f, max = 25f, step = 5, category = "Rift", subcategory = "mountaintop")
    var rotationSmoothness = 15f
}
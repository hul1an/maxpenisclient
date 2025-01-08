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


class MyConfig : Config(Mod("maxpenisaddons", ModType.SKYBLOCK), "maxpenisConfig.json") {
    @KeyBind(name = "Highlite Miner Keybind", category = "Rift", subcategory = "mountaintop")
    lateinit var minerKeyBind: OneKeyBind // had to move ts to the top because oneconfig in kotlin is strange

    init {
        initialize()
        minerKeyBind = OneKeyBind(UKeyboard.KEY_B)
        addDependency("subSwitch") { masterSwitch }
        addDependency("finalAge") { highliteMinerMainToggle }
        addDependency("rotationSmoothness") { highliteMinerMainToggle } // doesnt work
        addDependency("minerKeyBind") { highliteMinerMainToggle }
    }
    //test
    @Switch(name = "Master Switch", size = OptionSize.DUAL, category = "General", subcategory = "Switches")
    var masterSwitch: Boolean = false
    @Switch(name = "Sub Switch", size = OptionSize.DUAL, category = "General", subcategory = "Switches")
    var subSwitch: Boolean = false

    //rift
    @Switch(name = "Highlite Miner", size = OptionSize.DUAL, category = "Rift", subcategory = "mountaintop")
    var highliteMinerMainToggle: Boolean = false
    @Dropdown(name = "Final Age", size = OptionSize.DUAL, options = ["Youngite", "Timeite", "Obsolite"], category = "Rift", subcategory = "mountaintop")
    var finalAge = 0
    @Slider(name = "Rotation Smoothness", min = 10f, max = 50f, step = 5, category = "Rift", subcategory = "mountaintop")
    var rotationSmoothness = 20f
}
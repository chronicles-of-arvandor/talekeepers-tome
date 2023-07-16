package net.arvandor.talekeeper.rpkit

import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceName
import net.arvandor.talekeeper.ancestry.TtAncestry

class TtRpkAncestryWrapper(val ancestry: TtAncestry) : RPKRace {
    override fun getName(): RPKRaceName {
        return RPKRaceName(ancestry.name)
    }

    override fun getMinAge(): Int {
        return ancestry.minimumAge
    }

    override fun getMaxAge(): Int {
        return ancestry.maximumAge
    }
}

plugins {
    id("dev.tocraft.modmaster.forge")
}

dependencies {
    shadowCommon(implementation("dev.tocraft.crafted:cli:${rootProject.properties["cli_version"]}")!!)
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("META-INF/mods.toml") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

loom {
    forge {
        mixinConfigs.add("ctgen.mixins.json")
    }
}

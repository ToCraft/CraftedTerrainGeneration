plugins {
    id("dev.tocraft.modmaster.neoforge")
}

dependencies {
    shadowCommon(implementation("dev.tocraft:cli:${rootProject.properties["cli_version"]}")!!)
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

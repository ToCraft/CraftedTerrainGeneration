plugins {
    id("dev.tocraft.modmaster.fabric")
}


dependencies {
    shadowCommon(implementation("dev.tocraft.crafted:cli:${rootProject.properties["cli_version"]}")!!)
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>
    //inputs.properties.putAll(modMeta)

    filesMatching("fabric.mod.json") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

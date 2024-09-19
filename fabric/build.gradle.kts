plugins {
    id("dev.tocraft.modmaster.fabric")
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>
    //inputs.properties.putAll(modMeta)

    filesMatching("fabric.mod.json") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

dependencies {
    implementation("commons-cli:commons-cli:1.9.0")
}

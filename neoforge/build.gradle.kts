import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.tocraft.modmaster.neoforge")
}

dependencies {
    shadowCommon(implementation("dev.tocraft:cli:${rootProject.properties["cli_version"]}")!!)
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks.withType<ProcessResources> {
    @Suppress("UNCHECKED_CAST") val modMeta = parent!!.ext["mod_meta"]!! as Map<String, Any>

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(modMeta)
    }

    outputs.upToDateWhen { false }
}

tasks.getByName<RemapJarTask>("remapJar") {
    atAccessWideners.add("ctgen.accessWidener")
}
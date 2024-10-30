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

loom {
    runs {
        create("dataGen") {
            // use client and server code base
            client()
            server()

            // set vm args to use data generation
            name = "Data Generation"
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.modid=${rootProject.properties["archives_base_name"]}")
            vmArg("-Dfabric-api.datagen.output-dir=${parent!!.project("common").projectDir.resolve("src/main/generated")}")
        }
    }
}
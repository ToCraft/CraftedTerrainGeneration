plugins {
    id("dev.tocraft.modmaster.version")
}

allprojects {
    tasks.withType<Jar>().configureEach {
        manifest {
            attributes["Specification-Title"] = rootProject.properties["archives_base_name"]
            attributes["Specification-Vendor"] = "To_Craft"
            attributes["Implementation-Title"] = rootProject.properties["archives_base_name"]
            attributes["Implementation-Version"] = rootProject.version
            attributes["Implementation-Vendor"] = "To_Craft"
            attributes["Main-Class"] = "dev.tocraft.crafted.ctgen.runtime.Main"
        }
    }
}

ext {
    val modMeta = mutableMapOf<String, Any>()
    modMeta["minecraft"] = project.name
    modMeta["version"] = version
    set("mod_meta", modMeta)
}
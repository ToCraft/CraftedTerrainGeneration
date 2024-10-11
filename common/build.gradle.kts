plugins {
    id("dev.tocraft.modmaster.common")
}

dependencies {
    implementation("dev.tocraft.crafted:cli:${rootProject.properties["cli_version"]}")
}
sourceSets {
    main {
        resources {
            srcDir(rootDir.resolve("common/src/main/generated"))
        }
    }
}

plugins {
    id("dev.tocraft.modmaster.common")
}

dependencies {
    implementation("dev.tocraft:cli:${rootProject.properties["cli_version"]}")
}
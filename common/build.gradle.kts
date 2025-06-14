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

loom {
    accessWidenerPath = file("../../../common/src/main/resources/ctgen.accessWidener")
}

dependencies {
    implementation("dev.tocraft:cli:${rootProject.properties["cli_version"]}")
}
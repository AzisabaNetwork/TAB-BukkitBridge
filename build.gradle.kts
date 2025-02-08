plugins {
    java
    `maven-publish`
}

group = "net.azisaba"
version = "3.1.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")

    // TAB
    compileOnly(files("libs/TAB.v3.0.2.jar")) // https://github.com/NEZNAMY/TAB/releases/download/3.0.2/TAB.v3.0.2.jar

    // Providers
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.essentialsx:EssentialsX:2.19.2") {
        exclude("io.papermc", "paperlib")
    }
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(projectDir) { include("LICENSE") }
    }
}

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url = uri(
                if (project.version.toString().endsWith("SNAPSHOT"))
                    project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "https://repo.azisaba.net/repository/maven-snapshots/")
                else
                    project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "https://repo.azisaba.net/repository/maven-releases/")
            )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
//            artifact(tasks.getByName("jar"))
//            artifact(tasks.getByName("sourcesJar"))
//            artifact(tasks.getByName("javadocJar"))
        }
    }
}

plugins {
    java
    `maven-publish`
}

group = "net.azisaba"
version = "1.4.0"

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
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
    maven { url = uri("https://repo.kryptonmc.org/releases/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")

    // TAB
    compileOnly("me.neznamy:tab-api:3.0.2")

    // Providers
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.essentialsx:EssentialsX:2.19.2")
}

tasks {
    withType<ProcessResources> {
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
                    project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "")
                else
                    project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "")
            )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.getByName("jar"))
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))
        }
    }
}

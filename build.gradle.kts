plugins {
    java
}

group = "net.azisaba"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")

    // Providers
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.essentialsx:EssentialsX:2.19.2")
}
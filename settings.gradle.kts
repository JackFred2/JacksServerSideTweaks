import dev.kikugie.stonecutter.StonecutterSettings

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven {
			name = "Fabric"
			url = java.net.URI("https://maven.fabricmc.net/")
		}
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.4.4"
}

extensions.configure<StonecutterSettings> {
	kotlinController = true
	centralScript = "build.gradle.kts"

	shared {
		versions("1.21.1", "1.21.4")
		vcsVersion = "1.21.4"
	}

	create(rootProject)
}

rootProject.name = "jsst"

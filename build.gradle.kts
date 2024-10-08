plugins {
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("maven-publish")
}

version = "${properties["mod_version"]!!}+${stonecutter.current.version}"
group = properties["maven_group"]!!

// stonecutter constants
val mcVersion = stonecutter.current.version
val javaRequirement = if (stonecutter.eval(mcVersion, ">=1.20.5")) {
	21
} else {
	17
}

base {
	archivesName.set(properties["archive_name"].toString())
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("jsst") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	runConfigs.all {
		ideConfigGenerated(true)
		if (environment == "client") {
			programArgs("--username JackFred".split(" "))
			runDir = "../../run"
		} else {
			runDir = "../../runServer"
		}
	}
}

repositories {
	// Parchment Mappings
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
		content {
			includeGroup("org.parchmentmc.data")
		}
	}

	// Mod Menu, EMI
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/releases/")
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	// JackFredLib
	maven {
		name = "JackFredMaven"
		url = uri("https://maven.jackf.red/releases")
		content {
			includeGroupAndSubgroups("red.jackf")
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["deps.minecraft"]}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["deps.parchment"]}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${properties["deps.fabric_loader"]}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["deps.fabric_api"]}")

	modCompileOnly("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
	modLocalRuntime("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", project.version)
	inputs.property("javaRequirement", javaRequirement)
	inputs.property("mcVersion", mcVersion)

	filesMatching("fabric.mod.json") {
		expand(inputs.properties)
	}
}

java {
	withSourcesJar()
}

tasks.withType<JavaCompile> {
	options.release.set(javaRequirement)
}

tasks.named<Jar>("sourcesJar") {
	dependsOn(tasks.classes)
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		if (!System.getenv().containsKey("CI")) repositories.mavenLocal()
	}
}
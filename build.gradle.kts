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
	maven("https://maven.parchmentmc.org") {
		name = "ParchmentMC"
		content {
			includeGroup("org.parchmentmc.data")
		}
	}

	// Mod Menu, EMI
	maven("https://maven.terraformersmc.com/releases/") {
		name = "TerraformersMC"
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	// JackFredLib
	maven("https://maven.jackf.red/releases") {
		name = "JackFredMaven"
		content {
			includeGroupAndSubgroups("red.jackf")
		}
	}

	maven("https://maven.isxander.dev/releases") {
		name = "Xander Maven"
		content {
			includeGroupAndSubgroups("dev.isxander")
			includeGroupAndSubgroups("org.quiltmc")
		}
	}

	// server translations
	maven("https://maven.nucleoid.xyz") {
		name = "Nucleoid Maven"
		content {
			includeGroupAndSubgroups("xyz.nucleoid")
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["deps.minecraft"]}")
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["deps.parchment"]}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${properties["deps.fabric_loader"]}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["deps.fabric_api"]}")

	modImplementation("dev.isxander:yet-another-config-lib:${properties["deps.yacl"]}")

	include(modImplementation("red.jackf.jackfredlib:jackfredlib:${properties["deps.jackfredlib"]}")!!)
	include(modImplementation("xyz.nucleoid:server-translations-api:${properties["deps.servertranslations"]}")!!)
	include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${properties["deps.mixinextras"]}")!!)!!)

	modCompileOnly("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
	modLocalRuntime("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", project.version)
	inputs.property("javaRequirement", javaRequirement)
	inputs.property("mcVersion", mcVersion)

	// server translations
	from("src/main/resources/assets/jsst/lang") {
		into("data/jsst/lang")
	}

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
import me.modmuss50.mpp.ReleaseType

plugins {
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

val rawVersion = properties["mod_version"]!!.toString()
version = "$rawVersion+${stonecutter.current.version}"
group = properties["maven_group"]!!

// stonecutter constants
val mcVersion = stonecutter.current.version
val javaRequirement = 21

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
			includeGroupAndSubgroups("eu.pb4")
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
	include(modImplementation("eu.pb4:sgui:${properties["deps.sgui"]}")!!)
	include(modImplementation("xyz.nucleoid:server-translations-api:${properties["deps.servertranslations"]}")!!)
	include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:${properties["deps.mixinextras"]}")!!)!!)

	modCompileOnly("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
	modLocalRuntime("com.terraformersmc:modmenu:${properties["deps.modmenu"]}")
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", project.version)
	inputs.property("javaRequirement", javaRequirement)
	inputs.property("mcVersion", mcVersion)
	inputs.property("github_url", properties["project.github_url"]!!)

	// server translations
	from("../../src/main/resources/assets/jsst/lang") {
		into("data/jsst/lang")
	}

	// rename tag folders for lower versions
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
	duplicatesStrategy = DuplicatesStrategy.INCLUDE

	from("LICENSE") {
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

// configure mod publinsh
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	modLoaders.add("fabric")
	type.set(ReleaseType.STABLE)

	changelog.set("Changelog not written yet")

	dryRun.set(properties["project.dry_run"]!!.toString() == "true")

	displayName.set("${properties["project.name"]} ${project.version}")

	if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
		curseforge {
			accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))

			projectId.set(properties["project.curseforge_id"]!!.toString())
			projectSlug.set(properties["project.curseforge_slug"]!!.toString())
			minecraftVersions.addAll(properties["project.curseforge_versions"]!!.toString().split(","))

			javaVersions.add(JavaVersion.VERSION_21)
			serverRequired.set(true)

			requires("fabric-api", "yacl")
			optional("modmenu")
			embeds("server-translation-api")
		}
	}

	if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
		modrinth {
			accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))

			projectId.set(properties["project.modrinth_id"]!!.toString())
			minecraftVersions.addAll(properties["project.modrinth_versions"]!!.toString().split(","))

			requires("fabric-api", "yacl")
			optional("modmenu")
			embeds("server-translation-api")
		}
	}

	github {
		accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))

		additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)

		parent(rootProject.tasks.named("publishGithub"))
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

		if (System.getenv().containsKey("JF_MAVEN_USER") && System.getenv().containsKey("JF_MAVEN_PASS")) {
			maven {
				name = "JackFredMaven"
				url = uri("https://maven.jackf.red/releases")
				content {
					includeGroupAndSubgroups("red.jackf")
				}
				credentials {
					username = System.getenv()["JF_MAVEN_USER"]
					password = System.getenv()["JF_MAVEN_PASS"]
				}
			}
		}
	}
}
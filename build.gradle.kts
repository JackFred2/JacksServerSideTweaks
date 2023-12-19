@file:Suppress("UnstableApiUsage")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.4-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.2.1"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
}

// it CAN be null if not in a git repo
val grgit = runCatching { project.grgitService.service.get().grgit }.getOrNull()

fun getVersionSuffix(): String {
	return grgit?.branch?.current()?.name ?: "nogit+${properties["minecraft_version"]}"
}

group = properties["maven_group"]!!
version = "${properties["mod_version"]}+${getVersionSuffix()}"

base {
	archivesName.set("${properties["archives_base_name"]}")
}

repositories {
	mavenLocal()

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

	// Server Side Translations Lib
	maven {
		name = "NucleoidMC"
		url = uri("https://maven.nucleoid.xyz")
		content {
			includeGroup("fr.catcore")
			includeGroup("xyz.nucleoid")
		}
	}

	maven {
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
		content {
			includeGroup("me.lucko")
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

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("jsst") {
			sourceSet(sourceSets["main"])
		}
	}

	log4jConfigs.from(file("log4j2.xml"))

	runs {
		getByName("client") {
			environment = "client"
			configName = "Run Client"
			runDir = "run"
			source(sourceSets["client"])
			ideConfigGenerated(true)
			client()
		}

		getByName("server") {
			environment = "server"
			configName = "Run Server"
			runDir = "runServer"
			source(sourceSets["main"])
			ideConfigGenerated(true)
			server()
		}
	}
}

fun DependencyHandlerScope.modLocalImplementation(any: String, configure: ExternalModuleDependency.() -> Unit = {}) {
	modCompileOnly(any, configure)
	modLocalRuntime(any, configure)
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

	include(modImplementation("xyz.nucleoid:server-translations-api:${properties["server_translations_api_version"]}")!!)

	include(modImplementation("red.jackf.jackfredlib:jackfredlib-base:${properties["jflib_base_version"]}")!!)
    include(modImplementation("red.jackf.jackfredlib:jackfredlib-config:${properties["jflib_config_version"]}")!!)
    include(modImplementation("red.jackf.jackfredlib:jackfredlib-colour:${properties["jflib_colour_version"]}")!!)
    include(modImplementation("red.jackf.jackfredlib:jackfredlib-lying:${properties["jflib_lying_version"]}")!!)

	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric-api_version"]}")

	// COMPATIBILITY
	modLocalImplementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

	//modLocalRuntime("dev.emi:emi-fabric:${properties["emi_version"]}")
}

tasks.withType<ProcessResources>().configureEach {
	inputs.property("version", version)

	from("src/main/resources/assets/jsst/lang") {
		into("data/jsst/lang")
	}

	filesMatching("fabric.mod.json") {
		expand(inputs.properties)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

tasks.named<Jar>("sourcesJar") {
	dependsOn(tasks.classes)
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.WARN

	from("LICENSE") {
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

fun makeChangelogPrologue(): String {
	return """
		|Bundled:
		|  - Server Translations API: ${properties["server_translations_api_version"]}
		|  """.trimMargin()
}

println(makeChangelogPrologue())

val lastTagVal = properties["lastTag"]?.toString()
val newTagVal = properties["newTag"]?.toString()

var changelogText: Provider<String>
var changelogTask: TaskProvider<GenerateChangelogTask>? = null

changelogText = if (lastTagVal != null && newTagVal != null) {
	changelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
		lastTag.set(lastTagVal)
		newTag.set(newTagVal)
		prologue.set(makeChangelogPrologue())
		githubUrl.set(properties["github_url"]!!.toString())
		prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))
	}

	project.provider {
		return@provider changelogTask!!.get().changelogFile.get().asFile.readText()
	}
} else {
	project.provider { "Could not generate changelog." }
}

if (System.getenv().containsKey("GITHUB_TOKEN") && grgit != null) {
	tasks.named<GithubReleaseTask>("githubRelease") {
		authorization.set(System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" })
		body.set(changelogText)
		owner.set(properties["github_owner"]!!.toString())
		repo.set(properties["github_repo"]!!.toString())
		tagName.set(newTagVal)
		releaseName.set("${properties["mod_name"]} $newTagVal")
		targetCommitish.set(grgit.branch.current().name)
		releaseAssets.from(
			tasks["remapJar"].outputs.files,
			tasks["remapSourcesJar"].outputs.files,
		)

		changelogTask?.let {
			this@named.dependsOn(it)
		}
	}
}

tasks.named<DefaultTask>("publishMods") {
	changelogTask?.let { this.dependsOn(changelogTask) }
}

if (listOf("CURSEFORGE_TOKEN", "MODRINTH_TOKEN").any { System.getenv().containsKey(it) }) {
	publishMods {
		changelog.set(changelogText)
		type.set(when(properties["release_type"]) {
			"release" -> ReleaseType.STABLE
			"beta" -> ReleaseType.BETA
			else -> ReleaseType.ALPHA
		})
		modLoaders.add("fabric")
		modLoaders.add("quilt")
		file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)

		if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
			curseforge {
				projectId.set("850725")
				accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
				properties["game_versions_curse"]!!.toString().split(",").forEach {
					minecraftVersions.add(it)
				}
				displayName.set("${properties["prefix"]!!} ${properties["mod_name"]!!} ${version.get()}")
				listOf("fabric-api").forEach {
					requires {
						slug.set(it)
					}
				}
				listOf("server-translation-api").forEach {
					embeds {
						slug.set(it)
					}
				}
			}
		}

		if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
			modrinth {
				accessToken.set(System.getenv("MODRINTH_TOKEN"))
				projectId.set("eiYW74Yj")
				properties["game_versions_mr"]!!.toString().split(",").forEach {
					minecraftVersions.add(it)
				}
				displayName.set("${properties["mod_name"]!!} ${version.get()}")
				listOf("fabric-api").forEach {
					requires {
						slug.set(it)
					}
				}
			}
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"]!!)
		}
	}

	repositories {
		maven {
			name = "JackFredMaven"
			url = uri("https://maven.jackf.red/releases/")
			content {
				includeGroupByRegex("red.jackf.*")
			}
			credentials {
				username = properties["jfmaven.user"]?.toString() ?: System.getenv("JACKFRED_MAVEN_USER")
				password = properties["jfmaven.key"]?.toString() ?: System.getenv("JACKFRED_MAVEN_PASS")
			}
		}
	}
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
	mcVersion.set(properties["minecraft_version"]!!.toString())
	loader.set("fabric")
}
@file:Suppress("UnstableApiUsage", "RedundantNullableReturnType")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.ajoberstar.grgit.Grgit
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.5-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.2.1"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
}

val grgit: Grgit? = project.grgit

var canPublish = grgit != null && System.getenv("RELEASE") != null

fun getVersionSuffix(): String {
	return grgit?.branch?.current()?.name ?: "nogit+${properties["minecraft_version"]}"
}

group = properties["maven_group"]!!

if (System.getenv().containsKey("NEW_TAG")) {
	version = System.getenv("NEW_TAG").substring(1)
} else {
	val versionStr = "${properties["mod_version"]}+${properties["minecraft_version"]!!}"
	canPublish = false
	version = if (grgit != null) {
		"$versionStr+dev-${grgit.log()[0].abbreviatedId}"
	} else {
		"$versionStr+dev-nogit"
	}
}

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
			includeGroup("eu.pb4")
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

	runConfigs.configureEach {
		programArgs.addAll("--username JackFred".split(" "))
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
	include(modImplementation("eu.pb4:sgui:${properties["sgui_version"]}") {
		exclude(group = "net.fabricmc", module = "fabric-loader")
	})

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

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"]!!)
		}
	}

	repositories {
		// if not in CI we publish to maven local
		if (!System.getenv().containsKey("CI")) repositories.mavenLocal()

		if (canPublish) {
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
}

if (canPublish) {
	val lastTag = if (System.getenv("PREVIOUS_TAG") == "NONE") null else System.getenv("PREVIOUS_TAG")
	val newTag = "v$version"

	var generateChangelogTask: TaskProvider<GenerateChangelogTask>? = null

	// Changelog Generation
	if (lastTag != null) {
		val changelogHeader = if (properties.containsKey("changelogHeaderAddon")) {
			val addonProp: String = properties["changelogHeaderAddon"]!!.toString()

			if (addonProp.isNotBlank()) {
				addonProp + "\n\n"
			} else {
				""
			}
		} else {
			""
		}

		generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
			this.lastTag.set(lastTag)
			this.newTag.set(newTag)
			githubUrl.set(properties["github_url"]!!.toString())
			prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))

			// Add a bundled block for each module version
			prologue.set(changelogHeader + """
				|Bundled:
				|  - Server Translations API: ${properties["server_translations_api_version"]}
				|  - SGUI: ${properties["sgui_version"]}
				|  - JackFredLib: Base: ${properties["jflib_base_version"]}
				|  - JackFredLib: Config: ${properties["jflib_config_version"]}
				|  - JackFredLib: Colour: ${properties["jflib_colour_version"]}
				|  - JackFredLib: Lying: ${properties["jflib_lying_version"]}
				|  """.trimMargin())
		}
	}

	val changelogTextProvider = if (generateChangelogTask != null) {
		provider {
			generateChangelogTask!!.get().changelogFile.get().asFile.readText()
		}
	} else {
		provider {
			"No Changelog Generated"
		}
	}

	// GitHub Release
	tasks.named<GithubReleaseTask>("githubRelease") {
		generateChangelogTask?.let { dependsOn(it) }

		authorization = System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" }
		owner = properties["github_owner"]!!.toString()
		repo = properties["github_repo"]!!.toString()
		tagName = newTag
		releaseName = "${properties["mod_name"]} $newTag"
		targetCommitish = grgit!!.branch.current().name
		releaseAssets.from(
			tasks["remapJar"].outputs.files,
			tasks["remapSourcesJar"].outputs.files,
		)
		subprojects.forEach {
			releaseAssets.from(
				it.tasks["remapJar"].outputs.files,
				it.tasks["remapSourcesJar"].outputs.files,
			)
		}

		body = changelogTextProvider
	}

	// Mod Platforms
	if (listOf("CURSEFORGE_TOKEN", "MODRINTH_TOKEN").any { System.getenv().containsKey(it) }) {
		publishMods {
			changelog.set(changelogTextProvider)
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
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
	mcVersion.set(properties["minecraft_version"]!!.toString())
	loader.set("fabric")
}
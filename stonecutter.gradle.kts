import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.10-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}
stonecutter active "1.21.4" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublishGithub", stonecutter.chiseled) {
    group = "project"
    ofTask("publishGithub")

    dependsOn(rootProject.tasks.named("publishGithub"))
}

stonecutter registerChiseled tasks.register("chiseledPublishCurseforge", stonecutter.chiseled) {
    group = "project"
    ofTask("publishCurseforge")
}

stonecutter registerChiseled tasks.register("chiseledPublishModrinth", stonecutter.chiseled) {
    group = "project"
    ofTask("publishModrinth")
}

version = properties["mod_version"]!!.toString()

publishMods {
    type.set(ReleaseType.STABLE)
    changelog.set("Changelog not written yet")
    dryRun.set(properties["project.dry_run"]!!.toString() == "true")

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))

        repository.set(properties["project.github_repo"]!!.toString())
        commitish.set("v2/multiversion")
        tagName.set("v$version")

        displayName.set("${properties["project.name"]} ${project.version}")

        file.set(null as RegularFile?)

        allowEmptyFiles.set(true)
    }
}

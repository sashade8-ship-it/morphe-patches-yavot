group = "app.morphe.thirdparty.yavot"

patches {
    about {
        name = "YaVoT"
        description = "Independent Yandex voice-over translation add-on, compatible with Morphe Patches. " +
                "Requires patching alongside the official Morphe bundle."
        source = "https://github.com/sashade8-ship-it/morphe-patches-yavot"
        author = "YaVoT maintainers (sashade8-ship-it); original YaVoT: MarcaDian; ports: Jav1x and anddea"
        contact = "https://github.com/sashade8-ship-it/morphe-patches-yavot/issues"
        website = "https://github.com/sashade8-ship-it/morphe-patches-yavot"
        license = "GNU General Public License v3.0, with additional GPL section 7 requirements"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.register("patchListGeneratorClasspath")

dependencies {
    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)

    implementation(libs.morphe.patches.library)

    patchListGeneratorClasspath(libs.gson)

    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))
}

tasks {
    // The MPP is a distributable GPL work. Keep its notices with the artifact rather than
    // relying on the source checkout or a GitHub release page to provide them separately.
    withType<org.gradle.jvm.tasks.Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            into("META-INF")
            rename { "LICENSE" }
        }
        from(rootProject.file("NOTICE")) {
            into("META-INF")
            rename { "NOTICE" }
        }
    }

    named("sourcesJar") {
        enabled = false
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath.get()
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-parameters")
    }
}

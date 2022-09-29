import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev774"
    java
    `maven-publish`
}

val apiRelease by rootProject.extra { "1.6.15" }
val release by rootProject.extra { "1" }
group = "meteor"
version = "${apiRelease.split(".")[0]}.${apiRelease.split(".")[1]}${apiRelease.split(".")[2]}.$release"

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.compose.compiler:compiler")).apply {
            using(module("androidx.compose.compiler:compiler:1.3.1"))
        }
    }
}

repositories {
    mavenLocal()
    maven {url = uri("https://androidx.dev/storage/compose-compiler/repository")}
    maven { url = uri("https://raw.githubusercontent.com/open-osrs/hosting/master/") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev/")}
    google()
    mavenCentral()
    maven { url = uri("https://repo.runelite.net/") }
    maven { url = uri("https://raw.githubusercontent.com/MeteorLite/hosting/main/repo/") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev/") }
}

dependencies {
    // meteor-api
    implementation(project(":api-rs"))
    implementation(project(":api"))
    implementation(project(":http"))
    implementation(project(":annotations"))
    implementation(project(":logger"))
    runtimeOnly(project(":scripts"))
    runtimeOnly(files("./src/resources/injected-client.jar"))

    //Deob
    runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.70")

    //GPU
    implementation(group = "net.runelite.jocl", name = "jocl", version = "1.0")
    runtimeOnly(group = "net.runelite.jocl", name = "jocl", version = "1.0", classifier = "macos-x64")
    runtimeOnly(group = "net.runelite.jocl", name = "jocl", version = "1.0", classifier = "macos-arm64")
    implementation(group = "net.runelite", name = "rlawt", version = "1.3")
    implementation(group = "org.lwjgl", name = "lwjgl", version = "3.3.1")
    implementation(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.1", classifier = "natives-linux")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.1", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.1", classifier = "natives-windows-x86")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.1", classifier = "natives-macos")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.1", classifier = "natives-macos-arm64")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1", classifier = "natives-linux")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1", classifier = "natives-windows-x86")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1", classifier = "natives-macos")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.1", classifier = "natives-macos-arm64")

    //RuneLite Plugins
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.0")
    compileOnly(group= "org.projectlombok", name= "lombok", version= "1.18.20")
    annotationProcessor(group= "org.projectlombok", name= "lombok", version= "1.18.20")

    //Util
    implementation("org.rationalityfrontline:kevent:2.1.4")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.7.0")
    implementation(group = "com.google.guava", name = "guava", version = "30.1.1-jre")
    implementation(group = "org.apache.commons", name = "commons-text", version = "1.9")
    implementation(group = "commons-io", name = "commons-io", version = "2.11.0")
    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.7")
    implementation(group = "net.runelite", name = "discord", version = "1.4")
    implementation("com.formdev:flatlaf:2.4")
    implementation("com.formdev:flatlaf-intellij-themes:2.4")
    implementation("com.miglayout:miglayout:3.7.4")
    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("com.kitfox.svg:svg-salamander:1.0")
    implementation("com.formdev:flatlaf-extras:2.4")
    implementation ("com.godaddy.android.colorpicker:compose-color-picker-jvm:0.5.0")
    implementation("br.com.devsrsouza.compose.icons.jetbrains:octicons:1.0.0")

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
}

publishing {
    publications {
        create<MavenPublication>("mavenLocal") {
            artifactId = "client"
            version = "$apiRelease-$release"
            from(components["kotlin"])
        }
    }
}

tasks {
    processResources {
        dependsOn(":injector:inject")
        dependsOn(":scripts:assembleScripts")
    }
    compose.desktop {
        application {
            mainClass = "meteor.Main"
            nativeDistributions {
                version = "1.5.10"
                targetFormats(Exe, Deb)
                includeAllModules = true
                windows {
                    console = true
                    upgradeUuid = "9df19035-e962-4bb4-90c0-74330a07082b"
                    iconFile.set(project.file("src/main/resources/Meteor.ico"))
                }
            }
            jvmArgs(
                "-XX:+UseDynamicNumberOfGCThreads",
                "-XX:+UseZGC",
                "-Xmx1024m",
                "-ea",
                "-Dsun.java2d.uiScale=1.0",
                "--add-exports", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.net=ALL-UNNAMED",
                "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
                "--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED"
            )
        }
    }
    jar {
        archiveFileName.set("meteor-client-$apiRelease-r$release.jar")

        manifest {
            attributes(mutableMapOf("Main-class" to "meteor.Main"))
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    register<JavaExec>("createLauncherUpdate") {
        classpath(sourceSets["main"].runtimeClasspath)
        mainClass.set("meteor.launcher.CreateLauncherUpdate")
    }
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        apiVersion = "1.7"
        languageVersion = "1.7"
        jvmTarget = "17"
        // We can't use K2 yet due to using some kotlin compiler plugins which aren't supported yet.
        freeCompilerArgs = listOf( //"-Xuse-k2",
            "-Xjvm-default=all",
            "-Xbackend-threads=4")
    }
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    exclude("META-INF/BC1024KE.RSA", "META-INF/BC1024KE.SF", "META-INF/BC1024KE.DSA")
    exclude("META-INF/BC2048KE.RSA", "META-INF/BC2048KE.SF", "META-INF/BC2048KE.DSA")
    exclude("META-INF/KITFOX.SF", "META-INF/KITFOX.DSA")
}

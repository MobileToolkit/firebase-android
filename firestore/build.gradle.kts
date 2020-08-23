plugins {
    id("com.android.library")
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
    kotlin("android")
}

android {
    androidLibrary()
}

dependencies {
    implementation("androidx.appcompat:appcompat:${Versions.Androidx.APP_COMPAT}")
    implementation("androidx.recyclerview:recyclerview:${Versions.Androidx.RECYCLER_VIEW}")
    implementation("com.google.firebase:firebase-firestore:${Versions.Firebase.FIRESTORE}")
    implementation("org.mobiletoolkit.android.extensions:extensions-kotlin:1.2.0")

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestUtil("androidx.test:orchestrator:1.2.0")
}

publishing {
    publications {
        create<MavenPublication>("firestore") {
            from(components["android"])
            groupId = "${project.extra["groupId"]}"
            artifactId = "firestore"
            version = android.defaultConfig.versionName
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MobileToolkit/firebase-android")
            credentials {
                username = project.findProperty("gpr.githubUser") as String? ?: System.getenv("GITHUB_USER")
                password = project.findProperty("gpr.githubToken") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

bintray {
    user = project.findProperty("gpr.bintrayUser") as String? ?: System.getenv("BINTRAY_USER")
    key = project.findProperty("gpr.bintrayAPIKey") as String? ?: System.getenv("BINTRAY_API_KEY")
    dryRun = false
    override = false
    publish = true

    pkg.apply {
        repo = "public"
        name = project.name
        userOrg = "mobiletoolkit"
        setLicenses("MIT")
        vcsUrl = "${project.extra["vcsUrl"]}"

        version.apply {
            name = android.defaultConfig.versionName
            vcsTag = android.defaultConfig.versionName
            gpg.apply {
                sign = true
                passphrase = project.findProperty("gpr.bintrayGPGPassword") as String?
                    ?: System.getenv("BINTRAY_GPG_PASSWORD")
            }
        }
    }

    setPublications("firestore")
}

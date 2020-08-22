import org.jetbrains.kotlin.config.KotlinCompilerVersion

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
    implementation("org.mobiletoolkit.android.extensions:extensions-kotlin:1.0.1")
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestUtil("androidx.test:orchestrator:1.2.0")

    testImplementation(kotlin("test-junit", KotlinCompilerVersion.VERSION))
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
            url = uri("https://maven.pkg.github.com/MobileToolkit/firestore-android")
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


//plugins {
//    id 'com.jfrog.bintray' version '1.8.4'
//}
//
//apply plugin: 'com.android.library'
//apply plugin: 'digital.wup.android-maven-publish'
//apply plugin: 'kotlin-android'
//
//android {
//    compileSdkVersion rootProject.ext.compileSdkVersion
//    buildToolsVersion rootProject.ext.buildToolsVersion
//
//    defaultConfig {
//        minSdkVersion 16
//        targetSdkVersion 28
//        versionCode rootProject.ext.gitVersionCode
//        versionName rootProject.ext.gitVersionName
//        testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'
//        consumerProguardFiles 'proguard-rules.pro'
//    }
//    buildTypes {
//        release {
//            minifyEnabled false
//            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
//        }
//    }
//    compileOptions {
//        sourceCompatibility 1.8
//        targetCompatibility 1.8
//    }
//    lintOptions {
//        lintConfig file('lint.xml')
////        htmlReport true
////        htmlOutput file('lint-report.html')
//        abortOnError true
//        warningsAsErrors true
//    }
//}
//
//dependencies {
//    implementation "com.android.support:appcompat-v7:${rootProject.ext.androidSupportLibraryVersion}"
//    implementation "com.android.support:recyclerview-v7:${rootProject.ext.androidSupportLibraryVersion}"
//    implementation "com.android.support:support-v4:${rootProject.ext.androidSupportLibraryVersion}"
//    implementation 'com.google.firebase:firebase-firestore:21.5.0'
//    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.ext.kotlinVersion}"
//    implementation 'org.mobiletoolkit.android.extensions:extensions-kotlin:1.0.1'
//
//    androidTestImplementation 'com.android.support.test:runner:1.0.2'
//    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
//
//    testImplementation "org.jetbrains.kotlin:kotlin-test-junit:${rootProject.ext.kotlinVersion}"
//}

//archivesBaseName = "${rootProject.ext.groupId}.firestore"
//
//publishing {
//    publications {
//        firestore(MavenPublication) {
//            groupId = rootProject.ext.groupId
//            artifactId = 'firestore'
//            version = android.defaultConfig.versionName
//
//            from components.android
//        }
//    }
//}
//
//bintray {
//    user = rootProject.ext.bintrayUser
//    key = rootProject.ext.bintrayAPIKey
//    dryRun = false
//    override = false
//    publish = true
//    pkg {
//        repo = 'public'
//        name = project.name
//        userOrg = 'mobiletoolkit'
//        licenses = ['MIT']
//        vcsUrl = rootProject.ext.vcsUrl
//
//        version {
//            name = android.defaultConfig.versionName
//            released  = new Date()
//            vcsTag = android.defaultConfig.versionName
//            gpg {
//                sign = true
//                passphrase = rootProject.ext.bintrayGPGPassword
//            }
//        }
//    }
//    publications = ['firestore']
//}

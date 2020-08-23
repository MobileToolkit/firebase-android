buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
        classpath("digital.wup:android-maven-publish:3.6.2")
        classpath(kotlin("gradle-plugin", "1.4.0"))
    }
}

allprojects {
    val groupId by extra { "org.mobiletoolkit.android.firebase" }
    val vcsUrl by extra { "https://github.com/MobileToolkit/firebase-android.git" }
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/mobiletoolkit/public")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

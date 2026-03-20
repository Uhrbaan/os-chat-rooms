plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.5")
}

application {
    mainClass.set("os.chat.client.ChatClientWindow")
    applicationDefaultJvmArgs = listOf("-Dsun.java2d.uiScale=2") // Fix scaling
}

// If the server needs to be started separately
// tasks.register<JavaExec>("runServer") {
//     group = "application"
//     mainClass.set("os.chat.server.ClassWithMainFunction")
//     classpath = sourceSets["main"].runtimeClasspath
// }
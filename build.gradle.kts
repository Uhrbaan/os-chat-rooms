plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-validator:commons-validator:1.9.0")
}

// start client task 
val client = tasks.register<JavaExec>("client") {
    group = "application"
    description = "Runs the Chat Client"
    mainClass.set("os.chat.client.ChatClientWindow")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("-Dsun.java2d.uiScale=2") // Else looks waaaaaay to small on HDPI displays.
}

val server = tasks.register<JavaExec>("server") {
    group = "application"
    description = "Runs the Chat Server"
    mainClass.set("os.chat.server.ChatServerManager")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.named("run") {
    dependsOn(server, client)
    enabled = false // don't execute default application task
}

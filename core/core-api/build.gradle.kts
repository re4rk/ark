tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

dependencies {
    implementation(project(":core:core-enum"))
    implementation(project(":support:monitoring"))
    implementation(project(":support:logging"))
    implementation(project(":storage:db-core"))
    implementation(project(":clients:client-example"))
    implementation(project(":clients:client-s3"))

    testImplementation(project(":tests:api-docs"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

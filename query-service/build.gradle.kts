plugins {
  java
  application
  jacoco
  id("org.hypertrace.docker-java-application-plugin") version "0.8.1"
  id("org.hypertrace.docker-publish-plugin") version "0.8.1"
  id("org.hypertrace.integration-test-plugin")
  id("org.hypertrace.jacoco-report-plugin")
}

repositories {
  maven("http://packages.confluent.io/maven")
}

dependencies {
  constraints {
    runtimeOnly("io.netty:netty-codec-http2:4.1.60.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1020439")
    }
    runtimeOnly("io.netty:netty-handler-proxy:4.1.59.Final") {
      because("https://snyk.io/vuln/SNYK-JAVA-IONETTY-1020439s")
    }
    integrationTestRuntimeOnly("junit:junit:4.13.1") {
      because("https://snyk.io/vuln/SNYK-JAVA-JUNIT-1017047")
    }
  }

  implementation(project(":query-service-impl"))
  implementation("org.hypertrace.core.grpcutils:grpc-server-utils:0.3.0")
  implementation("org.hypertrace.core.serviceframework:platform-service-framework:0.1.22")
  implementation("io.grpc:grpc-netty:1.33.0")
  implementation("org.slf4j:slf4j-api:1.7.30")
  implementation("com.typesafe:config:1.4.0")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")

  implementation("com.typesafe:config:1.4.0")

  integrationTestImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
  integrationTestImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
  integrationTestImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
  integrationTestImplementation("org.testcontainers:testcontainers:1.15.2")
  integrationTestImplementation("org.testcontainers:junit-jupiter:1.15.2")
  integrationTestImplementation("org.testcontainers:kafka:1.15.2")
  integrationTestImplementation("org.hypertrace.core.serviceframework:integrationtest-service-framework:0.1.22")
  integrationTestImplementation("com.github.stefanbirkner:system-lambda:1.2.0")

  integrationTestImplementation("org.apache.kafka:kafka-clients:5.5.1-ccs")
  integrationTestImplementation("org.apache.kafka:kafka-streams:5.5.1-ccs")
  integrationTestImplementation("org.apache.avro:avro:1.10.1")
  integrationTestImplementation("org.hypertrace.core.datamodel:data-model:0.1.12")
  integrationTestImplementation("org.hypertrace.core.kafkastreams.framework:kafka-streams-serdes:0.1.13")

  integrationTestImplementation(project(":query-service-client"))
  integrationTestImplementation("org.hypertrace.core.attribute.service:attribute-service-client:0.8.7")
}

application {
  mainClass.set("org.hypertrace.core.serviceframework.PlatformServiceLauncher")
}

// Config for gw run to be able to run this locally. Just execute gw run here on Intellij or on the console.
tasks.run<JavaExec> {
  jvmArgs = listOf("-Dbootstrap.config.uri=file:${projectDir}/src/main/resources/configs", "-Dservice.name=${project.name}")
}

tasks.integrationTest {
  useJUnitPlatform()
}

hypertraceDocker {
  defaultImage {
    javaApplication {
      port.set(8090)
    }
  }
}

tasks.jacocoIntegrationTestReport {
  sourceSets(project(":query-service-impl").sourceSets.getByName("main"))
  sourceSets(project(":query-service-client").sourceSets.getByName("main"))
}
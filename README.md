# Gateway service example

1. Start a Spring boot app project:

2. Add these dependencies:

```
# For a Gradle project, in your buid.gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.1.7.RELEASE")
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

bootJar {
    baseName = 'gs-gateway'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
}

sourceCompatibility = 1.8
targetCompatibility = 1.8

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:Greenwich.SR2"
    }
}

dependencies {
    compile("org.springframework.cloud:spring-cloud-starter-gateway")
    compile("org.springframework.cloud:spring-cloud-starter-netflix-hystrix")
    compile("org.springframework.cloud:spring-cloud-starter-contract-stub-runner"){
        exclude group: "org.springframework.boot", module: "spring-boot-starter-web"
    }
    testCompile("org.springframework.boot:spring-boot-starter-test")
}
```

dependencies can be found here if needed:
 
https://spring.io/guides/gs/gateway/#scratch

3. Change your application.properties to an application.yml

4. Add the following to your application.yml:

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: dog # controller RequestMapping (route)
          uri: http://localhost:8081
          predicates:
            - Path=/dog/**
          filters:
            - AddResponseHeader=Access-Control-Allow-Origin, * # any headers
```

5. For a local profile, create an application-local.yml

```yml
server.port: 8080 # port you want this to run on

spring:
  cloud:
    gateway:
      routes:
        - id: dog
          uri: http://localhost:8081
          predicates:
            - Path=/dog/**
          filters:
            - AddResponseHeader=Access-Control-Allow-Origin, *
```

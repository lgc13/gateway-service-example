# Gateway service example

### Using spring.cloud.gateway

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

### Using zuul

1. Add the dependency:

```
# build.gradle file
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-zuul:2.2.1.RELEASE'
```

2. Add any configuration

```yml
zuul:
  routes:
    dog:
      path: /api/dog/**   # where requests are made to
      url: http://localhost:8081/dog  # where requests will be routed to
```

3. Add the annotation to your main class:

```java
@EnableZuulProxy
@SpringBootApplication
public class GatewayServiceExampleApplication {

	public static void main(String[] args) {
    		SpringApplication.run(GatewayServiceExampleApplication.class, args);
    	}
}
```

4. If you want a filter (and see logs), add the following:

(more notes here: https://spring.io/guides/gs/routing-and-filtering/)

```java
// SimpleFilter.java file

public class SimpleFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }

}

// And add a @Bean somwhere. maybe in main?

@Bean
public SimpleFilter simpleFilter() {
    return new SimpleFilter();
}
```

5. For CORS, you can add WebMvcConfigurer corsConfigurer:

```java
// mainFile.java

@EnableZuulProxy
@SpringBootApplication
public class GatewayServiceExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceExampleApplication.class, args);
	}

	@Bean
	public SimpleFilter simpleFilter() {
		return new SimpleFilter();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:3000")
						.allowedMethods("GET", "POST");
			}
		};
	}
}
```


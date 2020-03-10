# Gateway service example

### Using zuul

1. Add the dependency:

```groovy
// build.gradle file
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-zuul:2.2.1.RELEASE'
```

2. Add any configuration

```yaml
# application.yml
zuul:
  routes:
    dogs:
      path: /api/dogs/**   # where requests are made to
      url: http://localhost:8081/dogs  # where requests will be routed to
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
// Filter.java file
public class Filter extends ZuulFilter {

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

        log.info("{} request to {}", request.getMethod(), request.getRequestURL().toString());

        return null;
    }

}

// And add a @Bean. Possibly in a Config file

// CommonConfig.java
public class CommonConfig {

    @Bean
    public SimpleFilter simpleFilter() {
        return new SimpleFilter();
    }

}
```

5. For CORS, you can add WebMvcConfigurer corsConfigurer:

```java
// CommonConfig.java

@Configuration
public class CommonConfig {

	@Bean
	public SimpleFilter simpleFilter() {
		return new SimpleFilter();
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
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


### Using spring.cloud.gateway

1. Start a Spring boot app project:

2. Add these dependencies:

```groovy
// For a Gradle project, in your buid.gradle
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

```yaml
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

```yaml
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

### Spring Security

```groovy
// add dependencies:
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.security:spring-security-test'
```

- Implement WebSecurityConfigurerAdapter's configure:

```java
// SecurityConfigurer.java
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    private final MyUserDetailsService myUserDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
```

```java
// MyUserDetailsService.java
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User("myUsername", "myPassword", List.of());
    }
}
```

- Now if you try to access any endpoints here, it'll require this user. Try it on [localhost:8080](http://localhost:8080) (if you are on that port)

#### JWT tokens

```groovy
// add dependencies
compile group: 'io.jsonwebtoken', name: 'jjwt', version: '0.9.1'
compile group: 'javax.xml.bind', name: 'jaxb-api', version: '2.3.1'
```


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

- Implement UserDetailsService

```java
// MyUserDetailsService.java
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User("myUsername", "myPassword", List.of());  // here you could get your actual users from DB
    }
}
```

- Now if you try to access any endpoints here, it'll require this user. Try it on [localhost:8080](http://localhost:8080) (if you are on that port)

#### JWT tokens

##### Using this [video example](https://www.youtube.com/watch?v=X80nJ5T7YpE&t=575s)

```groovy
// add dependencies
compile group: 'io.jsonwebtoken', name: 'jjwt', version: '0.9.1'
compile group: 'javax.xml.bind', name: 'jaxb-api', version: '2.3.1'
```

- Create a JwtUtil

```java
// JwtUtil.java

@Component
public class JwtUtil {
    private String SECRET_KEY = "my secret key";

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String getUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Date getExpirationDate(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }
}
```

- Extend OncePerRequestFilter

```java
// JwtRequestFilter.java

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            username = jwtUtil.getUsername(jwtToken);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            if (jwtUtil.isTokenValid(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

- Add JwtRequestFilter to the SecurityConfigurer

```java
// SecurityConfigurer.java

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    // ...
    private final JwtRequestFilter jwtRequestFilter;

    // ...
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests().antMatchers("/authenticate").permitAll()
                .anyRequest().authenticated()
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
```

- Created endpoint to authenticate

```java
// TestController.java

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            ));
            log.info("User authenticated with username: {}", authenticationRequest.getUsername());
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            return ResponseEntity.ok(jwtUtil.generateToken(userDetails));
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", authenticationRequest.getUsername());
            return ResponseEntity.status(403).body("Invalid username or password");
        }
    }
}
```

##### Using Zuul
 
 - [article](https://www.baeldung.com/spring-security-zuul-oauth-jwt)
 
 


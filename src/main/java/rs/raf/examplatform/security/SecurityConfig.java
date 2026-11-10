package rs.raf.examplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableMethodSecurity public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthenticationFilter jwtFilter)throws Exception{
        return http.csrf(csrf->csrf.disable()).headers(h->h.frameOptions(f->f.sameOrigin()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a.requestMatchers("/api/auth/login","/h2-console/**").permitAll()
                .requestMatchers("/api/teacher/**").hasRole("TEACHER").requestMatchers("/api/student/**").hasRole("STUDENT")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();
    }
}

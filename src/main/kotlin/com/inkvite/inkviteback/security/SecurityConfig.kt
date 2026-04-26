package com.inkvite.inkviteback.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtConfig::class)
class SecurityConfig(
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtDecoder: JwtDecoder): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**").permitAll()
                it.requestMatchers("/artists/slug-available").permitAll()
                it.requestMatchers("/swagger-ui/**").permitAll()
                it.requestMatchers("/v3/api-docs/**").permitAll()
                it.requestMatchers(HttpMethod.POST, "/appointment/{slug}").permitAll()
                it.requestMatchers(HttpMethod.POST, "/appointment/{slug}/reference").permitAll()
                it.requestMatchers(HttpMethod.GET, "/appointment/verify").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.decoder(jwtDecoder) }
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun hmacSecretKey(jwtConfig: JwtConfig): SecretKeySpec =
        SecretKeySpec(Base64.getDecoder().decode(jwtConfig.secret), "HmacSHA256")

    @Bean
    fun jwtDecoder(secretKey: SecretKeySpec): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey).build()

    @Bean
    fun jwtEncoder(secretKey: SecretKeySpec): JwtEncoder {
        val jwk = OctetSequenceKey.Builder(secretKey).algorithm(JWSAlgorithm.HS256).build()
        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwk)))
    }
}

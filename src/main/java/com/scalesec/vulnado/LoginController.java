
<Only the complete Code with the correction>

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
     .csrf().disable()
     .cors()
     .and()
     .headers().frameOptions().disable()
     .and()
     .authorizeRequests()
     .antMatchers("/api/**").permitAll()
     .anyRequest().authenticated()
     .and()
     .formLogin()
     .and()
     .logout()
     .and()
     .build();
  }
}
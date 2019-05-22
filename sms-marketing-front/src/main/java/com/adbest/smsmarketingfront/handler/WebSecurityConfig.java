package com.adbest.smsmarketingfront.handler;

import com.adbest.smsmarketingfront.service.CustomerService;
import com.adbest.smsmarketingfront.service.impl.CustomerServiceImpl;
import com.adbest.smsmarketingfront.util.Current;
import com.adbest.smsmarketingfront.util.HttpTools;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import com.adbest.smsmarketingfront.util.ReturnMsgUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    CustomerService customerService;
    @Autowired
    CustomMetadataSource metadataSource;
    @Autowired
    UrlAccessDecisionManager urlAccessDecisionManager;
    @Autowired
    AuthenticationAccessDeniedHandler deniedHandler;
    @Autowired
    CustomAuthenticationProvider authenticationProvider;
    @Autowired
    private ReturnMsgUtil returnMsgUtil;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/index.html", "/static/**", "/nologin", "/favicon.ico", "/register", "/twilio/**", "/customer/getCode",
                "/customer/password", "/xls/**");
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setSecurityMetadataSource(metadataSource);
                        o.setAccessDecisionManager(urlAccessDecisionManager);
                        return o;
                    }
                })
                .and()
                .formLogin().loginPage("/nologin").loginProcessingUrl("/login")
                .usernameParameter("username").passwordParameter("password")
                .failureHandler(failureHandler())
                .successHandler(successHandler())
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authentication) throws IOException, ServletException {
                        resp.setContentType("application/json;charset=utf-8");
                        ReturnEntity respBean = ReturnEntity.success(returnMsgUtil.msg("LOGOUT_SUCCESS"));
                        ObjectMapper om = new ObjectMapper();
                        PrintWriter out = resp.getWriter();
                        out.write(om.writeValueAsString(respBean));
                        out.flush();
                        out.close();
                    }
                })
                .permitAll().and()
                .rememberMe().rememberMeParameter("rememberMe")
                .tokenValiditySeconds(60 * 60 * 24 * 7).tokenRepository(persistentTokenRepository()).userDetailsService(customerServiceImpl)
                .and().csrf().disable()
                .exceptionHandling().accessDeniedHandler(deniedHandler).and()
                .cors()
                .and()
                .csrf().disable();;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    protected AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) ->
                HttpTools.responseForJson(response, ReturnEntity.success(Current.get()));
    }

    @Bean
    protected AuthenticationFailureHandler failureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException e) ->
                HttpTools.responseForJson(response, ReturnEntity.fail(e.getMessage()));
    }
}
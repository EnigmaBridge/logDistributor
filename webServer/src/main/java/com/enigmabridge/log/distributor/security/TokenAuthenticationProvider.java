package com.enigmabridge.log.distributor.security;

import com.enigmabridge.log.distributor.api.ApiConfig;
import com.google.common.base.Optional;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class TokenAuthenticationProvider implements AuthenticationProvider {

    private TokenService tokenService;

    public TokenAuthenticationProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Optional<String> token = (Optional) authentication.getPrincipal();
        if (!token.isPresent() || token.get().isEmpty()) {
            throw new BadCredentialsException("Invalid token");
        }

        if (tokenService.containsManagementToken(token.get())){
            return new UsernamePasswordAuthenticationToken(token.get(), null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(ApiConfig.MANAGEMENT_ROLE));
        }

        if (tokenService.containsBusinessToken(token.get())){
            return new UsernamePasswordAuthenticationToken(token.get(), null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(ApiConfig.BUSINESS_ROLE));
        }

        throw new BadCredentialsException("Invalid token or token expired");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}

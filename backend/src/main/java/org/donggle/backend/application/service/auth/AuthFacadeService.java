package org.donggle.backend.application.service.auth;

import lombok.RequiredArgsConstructor;
import org.donggle.backend.application.service.request.OAuthAccessTokenRequest;
import org.donggle.backend.domain.oauth.SocialType;
import org.donggle.backend.infrastructure.oauth.kakao.dto.response.UserInfo;
import org.donggle.backend.ui.response.TokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacadeService {
    private final LoginClients oauthClients; //APi
    private final AuthService authService; //

    public String createAuthorizeRedirectUri(final String socialType, final String redirect_uri) {
        return oauthClients.redirectUri(SocialType.from(socialType), redirect_uri);
    }

    public TokenResponse login(final String socialType, final OAuthAccessTokenRequest request) {
        final UserInfo userInfo = oauthClients.findUserInfo(
                SocialType.from(socialType),
                request.code(),
                request.redirect_uri());
        return authService.login(userInfo);
    }

    public void logout(final Long memberId) {
        authService.logout(memberId);
    }

    public TokenResponse reissueAccessTokenAndRefreshToken(final Long memberId) {
        return authService.reissueAccessTokenAndRefreshToken(memberId);
    }
}

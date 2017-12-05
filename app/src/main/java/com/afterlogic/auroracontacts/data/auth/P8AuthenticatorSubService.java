package com.afterlogic.auroracontacts.data.auth;

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */
/*
class P8AuthenticatorSubService implements AuthenticatorSubService {

    private final P8AuthenticatorNetworkService service;

    @Inject
    P8AuthenticatorSubService(P8AuthenticatorNetworkService service) {
        this.service = service;
    }

    @Override
    public Single<AuthorizedAuroraSession> login(String host, String email, String pass) {
        return service.login(host, email, pass)
                .flatMap(authToken -> service.getUser(host, authToken.token)
                        .map(userInfo -> new AuthorizedData(authToken, userInfo))
                )
                .map(auth -> new AuthorizedAuroraSession(
                        auth.getUser().getPublicId(),
                        "APP_TOKEN_STUB",
                        auth.getToken(),
                        auth.getAccountId(),
                        email,
                        pass,
                        HttpUrl.parse(host),
                        Const.ApiVersion.API_P8
                ));
    }

    @Override
    public Single<AuthorizedAuroraSession> byToken(String host, String token) {

        return service.getUser(host, token)
                .map(userData -> new AuthorizedAuroraSession(
                        userData.second.getPublicId(),
                        "APP_TOKEN_STUB",
                        token,
                        userData.first,
                        null,
                        null,
                        HttpUrl.parse(host),
                        Const.ApiVersion.API_P8
                ));

    }

    @Override
    public Single<Boolean> isExternalClientLoginFormsAvailable(String host) {

        return service.checkExternalLoginFormsAvailable(host)
                .onErrorResumeNext(error -> {

                    if (error instanceof ApiResponseError) {

                        ApiResponseError apiError = (ApiResponseError) error;

                        if (apiError.getErrorCode() == ApiResponseError.MODULE_NOT_EXIST
                                || apiError.getErrorCode() == ApiResponseError.METHOD_NOT_EXIST) {

                            return Single.just(false);

                        }

                    }

                    return Single.error(error);

                });

    }

    @Override
    public Maybe<Integer> isApiHost(String host) {

        return service.ping(host)
                .map(pong -> true)
                .onErrorResumeNext(error -> isIncorrectApiVersionError(error) ?
                        Single.just(false): Single.error(error)
                )
                .toMaybe()
                .flatMap(isP8 -> isP8 ? Maybe.just(Const.ApiVersion.API_P8) : Maybe.empty());

    }

    private boolean isIncorrectApiVersionError(Throwable error) {
        return error instanceof JsonSyntaxException;
    }

    private class AuthorizedData {

        private final String token;
        private final UserP8 user;
        private final Long accountId;

        private AuthorizedData(AuthToken authToken, Pair<Long, UserP8> user) {
            this.token = authToken.token;
            this.user = user.second;
            this.accountId = user.first;
        }

        public String getToken() {
            return token;
        }

        public UserP8 getUser() {
            return user;
        }

        public Long getAccountId() {
            return accountId;
        }
    }

}
*/
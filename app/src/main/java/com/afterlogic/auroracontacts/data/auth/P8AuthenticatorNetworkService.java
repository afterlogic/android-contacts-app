package com.afterlogic.auroracontacts.data.auth;

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

/*
class P8AuthenticatorNetworkService {

    private final Api8 api;

    @Inject
    P8AuthenticatorNetworkService(Api8 api) {
        this.api = api;
    }

    Single<String> ping(String host){
        return api.ping(Api8.completeUrl(host))
                .compose(ApiUtil::checkResponseAndGetData);
    }

    Single<Pair<Long, UserP8>> getUser(String host, String token) {
        return api.getUser(Api8.completeUrl(host), token, new GetUserParametersDto(token))
                .compose(ApiUtil::checkResponse)
                .map(response -> new Pair<>(response.getAccountId(), response.getResult()));
    }

    Single<AuthToken> login(String host, String login, String password) {
        return api.login(Api8.completeUrl(host), new LoginParametersDto(login, password))
                .compose(ApiUtil::checkResponseAndGetData);
    }

    Single<Boolean> checkExternalLoginFormsAvailable(String host) {
        return api.checkExternalClientLoginFormAvailable(Api8.completeUrl(host))
                .compose(ApiUtil::checkResponseAndGetData);
    }

}
*/
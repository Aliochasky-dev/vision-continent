package com.VISION.continent.service;

import com.VISION.continent.dtos.NokashDto;
import com.VISION.continent.execption.VisionException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NokashPayoutService {

    private static final Logger log = LoggerFactory.getLogger(NokashPayoutService.class);

    private final RestTemplate restTemplate;

    @Value("${nokash.api.url}")
    private String apiUrl;

    @Value("${nokash.i-space-key}")
    private String iSpaceKey;

    @Value("${nokash.app-space-key}")
    private String appSpaceKey;

    @Value("${nokash.callback-base-url}")
    private String callbackUrl;

    /**
     * Étape 1 : génère la clé d'authentification pour le retrait.
     * Usage unique, expire après 2 minutes — à générer juste avant chaque payout,
     * jamais mise en cache.
     */
    private String genererAuthKey() {
        String url = apiUrl + "/lapas-on-trans/trans/auth"
                + "?i_space_key=" + iSpaceKey
                + "&app_space_key=" + appSpaceKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.postForEntity(url, entity, NokashDto.AuthResponse.class);

            NokashDto.AuthResponse authResponse = response.getBody();
            if (authResponse == null || !"LOGIN_SUCCESS".equals(authResponse.getStatus())) {
                throw new VisionException("Échec de l'authentification Nokash pour le retrait");
            }
            return authResponse.getData();

        } catch (RestClientException e) {
            throw new VisionException("Erreur de communication avec Nokash (auth) : " + e.getMessage());
        }
    }

    /**
     * Étape 2 : initie le payout Mobile Money, avec la clé d'auth en header.
     */
    public NokashDto.PayoutResponse initierPayout(String orderId, BigDecimal montant, String telephone, String paymentMethod) {

        String authKey = genererAuthKey();

        NokashDto.PayoutRequest requestBody = NokashDto.PayoutRequest.builder()
                .iSpaceKey(iSpaceKey)
                .appSpaceKey(appSpaceKey)
                .paymentType("CM_MOBILEMONEY")
                .country("CM")
                .paymentMethod(paymentMethod)
                .orderId(orderId)
                .amount(montant.toBigInteger().toString())
                .callbackUrl(callbackUrl)
                .userData(com.VISION.continent.dtos.NokashDto.PayinRequest.UserData.builder()
                        .userPhone(telephone)
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("auth-code", authKey);
        HttpEntity<NokashDto.PayoutRequest> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Payload payout envoyé à Nokash : order_id={}, amount={}, payment_method={}, user_phone={}",
                requestBody.getOrderId(), requestBody.getAmount(), requestBody.getPaymentMethod(), telephone);

        try {
            var response = restTemplate.postForEntity(
                    apiUrl + "/lapas-on-trans/trans/api-payout-request/407",
                    entity,
                    NokashDto.PayoutResponse.class);

            NokashDto.PayoutResponse body = response.getBody();

            log.debug("Réponse payout Nokash : status={}, message={}",
                    body != null ? body.getStatus() : "null",
                    body != null ? body.getMessage() : "null");

            if (body == null || !"REQUEST_OK".equals(body.getStatus())) {
                throw new VisionException("Nokash a refusé la requête de retrait : "
                        + (body != null ? body.getMessage() : "réponse vide"));
            }
            return body;

        } catch (RestClientException e) {
            throw new VisionException("Erreur de communication avec Nokash (payout) : " + e.getMessage());
        }
    }
}
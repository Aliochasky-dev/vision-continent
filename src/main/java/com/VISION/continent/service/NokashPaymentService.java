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

@Service
@RequiredArgsConstructor
public class NokashPaymentService {

    private static final Logger log = LoggerFactory.getLogger(NokashPaymentService.class);

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
     * Initie un payin Mobile Money auprès de Nokash.
     * paymentMethod attendu : "MTN_MOMO" ou "ORANGE_MONEY", selon le préfixe
     * du numéro (67/650-654/680-684 = MTN, 69/655-659/685-689 = Orange).
     *
     * Numéros de test sandbox Nokash :
     *   - 237690000000 + ORANGE_MONEY → SUCCESS garanti
     *   - autres numéros valides → peuvent renvoyer BALANCE_INSUFFICIENT (simulé)
     */
    public NokashDto.PayinResponse initierPayin(String orderId, BigDecimal montant, String telephone, String paymentMethod) {

        NokashDto.PayinRequest requestBody = NokashDto.PayinRequest.builder()
                .iSpaceKey(iSpaceKey)
                .appSpaceKey(appSpaceKey)
                .paymentType("CM_MOBILEMONEY")
                .country("CM")
                .paymentMethod(paymentMethod)
                .orderId(orderId)
                .amount(montant.toBigInteger().toString())
                .callbackUrl(callbackUrl)
                .userData(NokashDto.PayinRequest.UserData.builder()
                        .userPhone(telephone)
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NokashDto.PayinRequest> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Payload envoyé à Nokash : order_id={}, amount={}, payment_method={}, callback_url={}, user_phone={}",
                requestBody.getOrderId(), requestBody.getAmount(), requestBody.getPaymentMethod(),
                requestBody.getCallbackUrl(), requestBody.getUserData().getUserPhone());

        try {
            var response = restTemplate.postForEntity(
                    apiUrl + "/lapas-on-trans/trans/api-payin-request/407",
                    entity,
                    NokashDto.PayinResponse.class);

            NokashDto.PayinResponse body = response.getBody();

            log.debug("Réponse Nokash : status={}, message={}, data={}",
                    body != null ? body.getStatus() : "null",
                    body != null ? body.getMessage() : "null",
                    body != null && body.getData() != null ?
                            ("id=" + body.getData().getId() + ", status=" + body.getData().getStatus())
                            : "null");

            if (body == null || !"REQUEST_OK".equals(body.getStatus())) {
                throw new VisionException("Nokash a refusé la requête de paiement");
            }
            return body;

        } catch (RestClientException e) {
            throw new VisionException("Erreur de communication avec Nokash : " + e.getMessage());
        }
    }
}
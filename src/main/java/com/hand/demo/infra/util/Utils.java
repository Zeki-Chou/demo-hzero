package com.hand.demo.infra.util;

import io.choerodon.core.exception.CommonException;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public static String generateNStringMasking(int length, String maskCharacter) {
        return String.join("", Collections.nCopies(length, maskCharacter));
    }

    /**
     * make get request to get response entity for Iam response body.
     * throw error if response status not return 200
     * @param iamRemoteService iam remote service
     * @return iam response body
     */
    public static JSONObject getIamJSONObject(IamRemoteService iamRemoteService) {
        ResponseEntity<String> iamResponse = iamRemoteService.selectSelf();

        if (!iamResponse.getStatusCode().equals(HttpStatus.OK)) {
            throw new CommonException("Error getting iam object");
        }

        String responseBody = iamResponse.getBody();
        return new JSONObject(responseBody);
    }

    /**
     * get list of lov value from value set definition
     * @param lovAdapter lov adapter interface
     * @param lovCode lov code
     * @param organizationId tenant id
     * @return list of lov meaning defined in the lov code
     */
    public static List<String> getListLovValues(LovAdapter lovAdapter, String lovCode, Long organizationId) {
        return lovAdapter
                .queryLovValue(lovCode, organizationId)
                .stream()
                .map(LovValueDTO::getValue)
                .collect(Collectors.toList());
    }
}

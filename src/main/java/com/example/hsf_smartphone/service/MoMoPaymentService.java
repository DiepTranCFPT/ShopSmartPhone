package com.example.hsf_smartphone.service;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class MoMoPaymentService {
    private static final String PARTNER_CODE = "MOMOARSA20250415";
    private static final String ACCESS_KEY = "3CQaNEK6rEisH5Qr";
    private static final String SECRET_KEY = "zm4RKBUT1AqlTvMhteVAyl4xibDkSA5e";
    private static final String REQUEST_TYPE = "captureWallet";
    private static final String ENDPOINT = "https://payment.momo.vn/v2/gateway/api/create";
    private static final String RETURN_URL = "http://116.103.110.134:8080/wallet/momo-return";
    private static final String NOTIFY_URL = "http://116.103.110.134:8080/wallet/momo-notify";

    public Map<String, Object> createPaymentRequest(double amount, String orderId, String orderInfo, String requestType, Map<String, Object> extraParams) {
        try {
            String requestId = orderId + System.currentTimeMillis();
            String amountStr = String.valueOf((long) amount);
            String extraData = extraParams != null && extraParams.containsKey("extraData") ? (String) extraParams.get("extraData") : "";
            Map<String, Object> params = new HashMap<>();
            params.put("partnerCode", PARTNER_CODE);
            params.put("accessKey", ACCESS_KEY);
            params.put("requestId", requestId);
            params.put("amount", amountStr);
            params.put("orderId", orderId);
            params.put("orderInfo", orderInfo);
            params.put("redirectUrl", RETURN_URL);
            params.put("ipnUrl", NOTIFY_URL);
            params.put("lang", "vi");
            params.put("extraData", extraData);
            params.put("requestType", requestType);
            // Thêm các trường production động
            if (extraParams != null) {
                if (extraParams.containsKey("items")) params.put("items", extraParams.get("items"));
                if (extraParams.containsKey("userInfo")) params.put("userInfo", extraParams.get("userInfo"));
                if (extraParams.containsKey("partnerName")) params.put("partnerName", extraParams.get("partnerName"));
                if (extraParams.containsKey("storeId")) params.put("storeId", extraParams.get("storeId"));
                if (extraParams.containsKey("subPartnerCode")) params.put("subPartnerCode", extraParams.get("subPartnerCode"));
                if (extraParams.containsKey("orderGroupId")) params.put("orderGroupId", extraParams.get("orderGroupId"));
                if (extraParams.containsKey("autoCapture")) params.put("autoCapture", extraParams.get("autoCapture"));
            }
            // Tạo signature đúng chuẩn production (sort a-z)
            String rawHash = buildSignatureData(params);
            String signature = hmacSHA256(rawHash, SECRET_KEY);
            params.put("signature", signature);
            return params;
        } catch (Exception e) {
            throw new RuntimeException("MoMo payment request creation failed", e);
        }
    }

    // Sửa lại hàm tạo signature đúng chuẩn production (sort a-z các trường)
    // Dùng cho requestType = payWithVTS
    public static String buildSignatureData(Map<String, Object> params) {
        // Sắp xếp các trường theo thứ tự a-z đúng chuẩn MoMo
        StringBuilder sb = new StringBuilder();
        sb.append("accessKey=").append(params.get("accessKey"));
        sb.append("&amount=").append(params.get("amount"));
        sb.append("&extraData=").append(params.get("extraData"));
        sb.append("&ipnUrl=").append(params.get("ipnUrl"));
        sb.append("&orderId=").append(params.get("orderId"));
        sb.append("&orderInfo=").append(params.get("orderInfo"));
        sb.append("&partnerCode=").append(params.get("partnerCode"));
        sb.append("&redirectUrl=").append(params.get("redirectUrl"));
        sb.append("&requestId=").append(params.get("requestId"));
        sb.append("&requestType=").append(params.get("requestType"));
        return sb.toString();
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Xác thực callback MoMo (redirectUrl, ipnUrl) bằng signature
     */
    public boolean verifyMomoCallbackSignature(Map<String, String> params) {
        String rawData = "accessKey=" + params.getOrDefault("accessKey", "")
            + "&amount=" + params.getOrDefault("amount", "")
            + "&extraData=" + params.getOrDefault("extraData", "")
            + "&message=" + params.getOrDefault("message", "")
            + "&orderId=" + params.getOrDefault("orderId", "")
            + "&orderInfo=" + params.getOrDefault("orderInfo", "")
            + "&orderType=" + params.getOrDefault("orderType", "")
            + "&partnerCode=" + params.getOrDefault("partnerCode", "")
            + "&payType=" + params.getOrDefault("payType", "")
            + "&requestId=" + params.getOrDefault("requestId", "")
            + "&responseTime=" + params.getOrDefault("responseTime", "")
            + "&resultCode=" + params.getOrDefault("resultCode", "")
            + "&transId=" + params.getOrDefault("transId", "");
        try {
            String expectedSignature = hmacSHA256(rawData, SECRET_KEY);
            return expectedSignature.equals(params.get("signature"));
        } catch (Exception e) {
            return false;
        }
    }
}

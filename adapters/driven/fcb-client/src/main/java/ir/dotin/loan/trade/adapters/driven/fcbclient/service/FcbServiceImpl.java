package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import com.thoughtworks.xstream.XStream;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.client.FcbFeignClient;
import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbConfiguration;
import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbXStreamFactory;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;

import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbServiceImpl implements FcbService {

    private final FcbFeignClient fcbFeignClient;
    private final FcbBaseRequestBuilder requestBuilder;
    private final FcbConfiguration fcbConfiguration;
    private final XStream xStream = new XStream();

    @Override
    public <T> Result<T> executeUsecase(FcbRequest request, Class<T> responseClass) {
        try {
            log.debug("Executing FCB usecase: {}", request);

            // Convert request to XML string
            String usecaseListXML = marshalToXml(request);
            log.debug("Request XML (usecaseListXML parameter): {}", usecaseListXML);

            try (Response response = fcbFeignClient.executeUseCase(
                    usecaseListXML, fcbConfiguration.isShowExceptions(), fcbConfiguration.isSameSession(), true)) {

                return processResponse(response, responseClass);
            }
        } catch (Exception e) {
            log.error("An unexpected integration error occurred executing FCB usecase", e);
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, e.getMessage()));
        }
    }

    private String marshalToXml(FcbRequest request) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(request.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        StringWriter writer = new StringWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        marshaller.marshal(request, writer);

        return writer.toString();
    }

    private <T> Result<T> processResponse(Response response, Class<T> responseClass) throws Exception {
        int status = response.status();
        log.debug("Response status: {}", status);
        log.debug("Response headers: {}", response.headers());

        if (status != 200) {
            log.error("Unexpected status code: {}", status);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Service returned status: " + status));
        }

        // Check if body exists
        if (response.body() == null) {
            log.error("Response body is null");
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Empty response from FCB service"));
        }

        // Read response body
        byte[] bodyBytes = response.body().asInputStream().readAllBytes();
        log.debug("Response body length: {} bytes", bodyBytes.length);

        if (bodyBytes.length == 0) {
            log.error("Response body is empty (0 bytes)");

            Collection<String> location = response.headers().get("location");
            if (location != null && !location.isEmpty()) {
                log.error("Service returned redirect to: {}", location);
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_AUTHENTICATION_FAILED,
                        "Service redirected - authentication may be required"));
            }

            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Empty response from FCB service"));
        }

        String xmlResponse = new String(bodyBytes, StandardCharsets.UTF_8);
        log.debug("FCB response XML: {}", xmlResponse);

        String trimmedResponse = xmlResponse.trim();

        if (!trimmedResponse.startsWith("<")) {
            log.error("Response is not XML. Content: {}", xmlResponse);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Invalid response format from FCB service"));
        }

        return parseAndValidateResponse(xmlResponse, responseClass);
    }

    private <T> Result<T> parseAndValidateResponse(String xmlResponse, Class<T> responseClass) {
        try {
            XStream xstream = FcbXStreamFactory.createXStream();
            xstream.processAnnotations(responseClass);

            @SuppressWarnings("unchecked")
            T parsedResponse = (T) xstream.fromXML(xmlResponse);

            if (parsedResponse instanceof FcbBaseResponse baseResponse) {
                if (!baseResponse.getErrorMessage().isEmpty()) {
                    String errorDesc = baseResponse.getErrorDescription();
                    log.error(
                            "FCB business error: rsCode={}, transactionCode={}, error={}",
                            baseResponse.getRsCode(),
                            baseResponse.getTransactionCode(),
                            errorDesc);

                    FcbBusinessLocalizedMessageCodes errorCode;
                    if ("EXCEPTION".equalsIgnoreCase(baseResponse.getRsCode())) {
                        errorCode = FcbBusinessLocalizedMessageCodes.FCB_BUSINESS_EXCEPTION;
                    } else {
                        errorCode = FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE;
                    }

                    return Result.failure(Notification.ofError(errorCode, errorDesc));
                }

                log.debug(
                        "FCB business response successful: rsCode={}, transactionCode={}",
                        baseResponse.getRsCode(),
                        baseResponse.getTransactionCode());
            }

            return Result.success(parsedResponse);

        } catch (Exception e) {
            log.error("Error parsing XML response", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Failed to parse response: " + e.getMessage()));
        }
    }
}

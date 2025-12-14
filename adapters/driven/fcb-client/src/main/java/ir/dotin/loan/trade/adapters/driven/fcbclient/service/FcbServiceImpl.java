package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import com.thoughtworks.xstream.XStream;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.client.FcbHttpClient;
import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbConfiguration;
import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbXStreamFactory;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.FcbErrorCodeMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbServiceImpl implements FcbService {

    private final FcbHttpClient fcbHttpClient;
    private final FcbBaseRequestBuilder requestBuilder;
    private final FcbConfiguration fcbConfiguration;

    @Override
    public <T> Result<T> executeUsecase(FcbRequest request, Class<T> responseClass, FcbContext context) {
        try {
            String usecaseListXML = marshalToXml(request);
            log.debug("Generated request XML: {}", usecaseListXML);

            ResponseEntity<@NonNull String> response = fcbHttpClient.executeUseCase(
                    usecaseListXML,
                    fcbConfiguration.integration().showExceptions(),
                    fcbConfiguration.integration().sameSession(),
                    true);

            return processResponse(response, responseClass, context);

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

    private <T> Result<T> processResponse(
            ResponseEntity<@NonNull String> response, Class<T> responseClass, FcbContext context) {
        try {
            int status = response.getStatusCode().value();
            log.debug("Response status: {}", status);
            log.debug("Response headers: {}", response.getHeaders());

            if (status != 200) {
                log.error("Unexpected status code: {}", status);
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Service returned status: " + status));
            }

            String responseXml = response.getBody();

            if (responseXml == null || responseXml.isEmpty()) {
                log.error("Response body is empty");

                String location = response.getHeaders().getLocation() != null
                        ? response.getHeaders().getLocation().toString()
                        : null;

                if (location != null) {
                    log.error("Service returned redirect to: {}", location);
                    return Result.failure(Notification.ofError(
                            FcbBusinessLocalizedMessageCodes.FCB_AUTHENTICATION_FAILED,
                            "Service redirected - authentication may be required"));
                }

                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Empty response from FCB service"));
            }

            log.debug("Response body length: {} characters", responseXml.length());
            log.debug("FCB response XML: {}", responseXml);

            String trimmedResponse = responseXml.trim();

            if (!trimmedResponse.startsWith("<")) {
                log.error("Response is not XML. Content: {}", responseXml);
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR,
                        "Invalid response format from FCB service"));
            }

            return parseAndValidateResponse(trimmedResponse, responseClass, context);

        } catch (Exception e) {
            log.error("Error processing response", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR,
                    "Failed to process response: " + e.getMessage()));
        }
    }

    private <T> Result<T> parseAndValidateResponse(String xmlResponse, Class<T> responseClass, FcbContext context) {
        try {
            XStream xstream = FcbXStreamFactory.createXStream();
            xstream.processAnnotations(responseClass);

            @SuppressWarnings("unchecked")
            T parsedResponse = (T) xstream.fromXML(xmlResponse);

            if (parsedResponse instanceof FcbBaseResponse baseResponse) {
                if (baseResponse.getErrorMessage() != null
                        && !baseResponse.getErrorMessage().isEmpty()) {
                    Notification errorNotification = FcbErrorCodeMapper.mapRsCodeToNotification(baseResponse, context);
                    return Result.failure(errorNotification);
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

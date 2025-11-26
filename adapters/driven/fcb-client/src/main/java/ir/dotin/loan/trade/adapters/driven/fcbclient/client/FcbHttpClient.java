package ir.dotin.loan.trade.adapters.driven.fcbclient.client;

import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface FcbHttpClient {

    @PostExchange(url = "/httpUsecaseRunner", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<@NonNull String> executeUseCase(
            @RequestParam("usecaseXML") String usecaseListXML,
            @RequestParam("showExceptions") boolean showExceptions,
            @RequestParam("sameSession") boolean sameSession,
            @RequestParam("utf8") boolean utf8);
}

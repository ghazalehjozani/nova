package ir.dotin.loan.trade.adapters.driven.fcbclient.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FeignConfiguration;

import feign.Response;

@FeignClient(name = "fcb-client", url = "${fcb.integration.base-url}", configuration = FeignConfiguration.class)
public interface FcbFeignClient {

    @PostMapping(value = "/httpUsecaseRunner", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Response executeUseCase(
            @RequestParam(value = "usecaseXML") String usecaseListXML,
            @RequestParam(value = "showExceptions") boolean showExceptions,
            @RequestParam(value = "sameSession") boolean sameSession,
            @RequestParam(value = "utf8") boolean utf8);
}

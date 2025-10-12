package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
public class FcbRequest {

    @XmlAttribute
    @Builder.Default
    private String version = "2";

    @XmlElement(name = "usecases")
    private Usecases usecase;
}

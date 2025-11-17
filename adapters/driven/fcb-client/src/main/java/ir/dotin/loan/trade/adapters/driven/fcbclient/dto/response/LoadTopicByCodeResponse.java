package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoadTopicByCodeResponse extends FcbBaseResponse {

    @XStreamImplicit(itemFieldName = "TopicInfoDTO")
    private List<TopicResponse> topicResponses;
}

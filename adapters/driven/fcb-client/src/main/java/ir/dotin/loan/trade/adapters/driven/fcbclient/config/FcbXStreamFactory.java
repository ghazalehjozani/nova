package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositClosedResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateCreditorDepositResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateDebtorDepositResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FcbXStreamFactory {

    public static XStream createXStream() {
        XStream xstream = new XStream();

        xstream.addPermission(NoTypePermission.NONE);

        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);

        xstream.allowTypesByWildcard(
                new String[] {"ir.dotin.loan.trade.adapters.driven.fcbclient.dto.**", "java.util.**", "java.lang.**"});
        xstream.alias("ir.dotin.lc.dto.ilccredit.bill.ElectronicBillAccountVO", OpenAccountResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.DepositInfoDTO", DepositInfoResponse.class);

        xstream.alias("ir.dotin.lc.dto.ilccredit.bill.ElectronicBillIssueDocumentVO", IssueDocumentResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.EconomicalSectionDTO", EconomicalSectionResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.ValidationResultDTO", FcbValidationResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.DepositClosedResultDTO", DepositClosedResponse.class);

        xstream.alias(
                "com.fanap.business.cmplexpenditure.dto.ValidateDebtorDepositResultDTO",
                ValidateDebtorDepositResponse.class);

        xstream.alias(
                "com.fanap.business.cmplexpenditure.dto.ValidateCreditorDepositResultDTO",
                ValidateCreditorDepositResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.CustomerInfoResultDTO", CustomerInfoResponse.class);

        xstream.autodetectAnnotations(true);

        xstream.ignoreUnknownElements();
        return xstream;
    }
}

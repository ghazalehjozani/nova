package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.AccountInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.AssuranceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.BranchResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CancelTransferMoneyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerBirthInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositClosedResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.HasAllowedCurrencyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReasonTypeResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ResourceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.TopicResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.TransferMoneyResponse;
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

        xstream.allowTypesByWildcard(new String[] {
            "ir.dotin.loan.trade.adapters.driven.fcbclient.dto.**", "java.util.**", "java.lang.**", "com.fanap.**"
        });

        xstream.alias("com.fanap.service.customer.serviceobjects.SharedAddress", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.SharedJob", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.SharedPhone", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.SharedCustomerActivities", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.Job", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.Phone", Object.class);
        xstream.alias("com.fanap.service.customer.serviceobjects.CustomerActivity", Object.class);
        xstream.alias("com.fanap.business.deposit.valueobjects.GeneralVO", Object.class);
        xstream.alias(
                "com.fanap.business.lc.valueobjects.ilccredit.bill.ElectronicBillAccountVO", OpenAccountResponse.class);

        xstream.alias("ir.dotin.lc.dto.ilccredit.bill.ElectronicBillAccountVO", OpenAccountResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.DepositInfoDTO", DepositInfoResponse.class);
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
        xstream.alias(
                "com.fanap.business.deposit.service.valueobjects.TransferMoneyReturnVO", TransferMoneyResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.ReasonTypeDTO", ReasonTypeResponse.class);
        xstream.alias(
                "com.fanap.business.cmplexpenditure.dto.HasAllowedCurrencyResultDTO", HasAllowedCurrencyResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.TopicInfoDTO", TopicResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.ResourceDTO", ResourceResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.CustomerInfoResultDTO", CustomerBirthInfoResponse.class);
        xstream.alias(
                "com.fanap.business.cmplexpenditure.dto.CancelTransferMoneyDTO", CancelTransferMoneyResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.BranchDTO", BranchResponse.class);

        xstream.alias("com.fanap.business.cmplexpenditure.dto.SharedAccountResult", AccountInfoResponse.class);
        xstream.alias("com.fanap.business.cmplexpenditure.dto.AssuranceDTO", AssuranceResponse.class);
        xstream.alias(
                "com.fanap.business.loan.valueobjects.assurance.ChequeInformationVO",
                AssuranceResponse.ChequeInformationVO.class);
        xstream.alias(
                "com.fanap.business.loan.valueobjects.assurance.PromissoryNoteInfoVO",
                AssuranceResponse.PromissoryNoteInfoVO.class);
        xstream.alias(
                "com.fanap.business.loan.valueobjects.assurance.AttachedDepositDTO",
                AssuranceResponse.AttachedDepositDTO.class);
        xstream.alias(
                "com.fanap.service.complementary.assurance.output.AssuranceTypeDTO",
                AssuranceResponse.AssuranceTypeDTO.class);

        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();

        return xstream;
    }
}

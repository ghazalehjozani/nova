package ir.dotin.loan.trade.architecture;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RedrivableInboxOpsContractTest {

    // Mirrors FacilityReconMapping.REDRIVABLE_INBOX_OPS (package-private in the driven-reconciliation module). The
    // recon
    // adapter must not depend on the driving-contract module, so the wire codes are duplicated there as literals; this
    // guard fails if a code stops resolving (a rename would silently re-deaden the INV-7 inbox re-drive path) or if a
    // new FCB inbox op is added without a conscious redrivability decision.
    private static final Set<String> REDRIVABLE_INBOX_OPS_MIRROR = Set.of(
            "installment.collection",
            "installment.collection.compensate",
            "loanFacility.cancel",
            "facility.close.paid.off",
            "facility.cancel.close.paid.off",
            "loanFacility.restructuring",
            "collateral.update");

    @Test
    void every_redrivable_inbox_op_resolves_to_a_real_fcb_wire_code() {
        for (String code : REDRIVABLE_INBOX_OPS_MIRROR) {
            assertDoesNotThrow(
                    () -> FcbEventOperationType.ofCode(code),
                    "redrivable inbox op code '" + code + "' must resolve to an FcbEventOperationType");
        }
    }

    @Test
    void every_fcb_inbox_op_has_an_explicit_redrivability_decision() {
        Set<String> allWireCodes = Arrays.stream(FcbEventOperationType.values())
                .map(FcbEventOperationType::getCode)
                .collect(Collectors.toUnmodifiableSet());
        assertEquals(
                REDRIVABLE_INBOX_OPS_MIRROR,
                allWireCodes,
                "a new FcbEventOperationType must be consciously added to (or excluded from) the recon redrivable "
                        + "mirror — update FacilityReconMapping.REDRIVABLE_INBOX_OPS and this mirror together (INV-7)");
    }
}

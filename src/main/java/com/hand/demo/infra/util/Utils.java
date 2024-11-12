package com.hand.demo.infra.util;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.PurchaseStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utils
 */
public class Utils {
    private Utils() {}

    public static String generateNStringMasking(int length, String maskCharacter) {
        return String.join("", Collections.nCopies(length, maskCharacter));
    }

    public static boolean validPurchaseStatus(String status) {
        for (PurchaseStatus s: PurchaseStatus.values()) {
            if (s.name().equals(status)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * calculate the total, tax, and exclude tax amount from invoice apply lines
     * if line object in the list has line id, adjust to the difference
     * otherwise add the amount to the header normally
     * @param invoiceApplyLines list of invoice lines
     * @param invoiceApplyHeader invoice header object
     * @param invoiceApplyLinesFromDb list of invoice lines related to the header
     */
    public static void addAmountFromLineList(
            List<InvoiceApplyLine> invoiceApplyLines,
            InvoiceApplyHeader invoiceApplyHeader,
            List<InvoiceApplyLine> invoiceApplyLinesFromDb)
    {
        InvoiceApplyHeader header = new InvoiceApplyHeader();

        header.setTotalAmount(BigDecimal.ZERO);
        header.setTaxAmount(BigDecimal.ZERO);
        header.setExcludeTaxAmount(BigDecimal.ZERO);

        List<InvoiceApplyLine> insertLine = new ArrayList<>();
        // contains lines only from db
        List<InvoiceApplyLine> updateLine = new ArrayList<>();

        // track update line from the request
        Map<Long, InvoiceApplyLine> updateLineMap = new HashMap<>();

        for (InvoiceApplyLine line: invoiceApplyLines) {
            if (line.getApplyLineId() != null) {
                updateLineMap.put(line.getApplyLineId(), line);
            } else {
                insertLine.add(line);
            }
        }

        for (InvoiceApplyLine line: invoiceApplyLinesFromDb) {
            if (updateLineMap.containsKey(line.getApplyLineId())) {
                updateLine.add(line);
            } else {
                insertLine.add(line);
            }
        }

        // for insert, just add the amount to the header
        insertLine.stream().map(Utils::calculateAmountLine).forEach(amountInvoiceLine -> {
            BigDecimal sumTotalAmount = header
                    .getTotalAmount()
                    .add(amountInvoiceLine.getTotalAmount());
            BigDecimal sumTaxAmount = header
                    .getTaxAmount()
                    .add(amountInvoiceLine.getTaxAmount());
            BigDecimal sumExcludeTaxAmount = header
                    .getExcludeTaxAmount()
                    .add(amountInvoiceLine.getExcludeTaxAmount());

            header.setTotalAmount(sumTotalAmount);
            header.setTaxAmount(sumTaxAmount);
            header.setExcludeTaxAmount(sumExcludeTaxAmount);
        });

        // for update, add old amount and add the difference
        // between new and old invoice line
        updateLine.forEach(invoiceApplyLine -> {
            InvoiceApplyLine current = updateLineMap.get(invoiceApplyLine.getApplyLineId());
            InvoiceApplyLine currentAmount = calculateAmountLine(current);
            InvoiceApplyLine diff = invoiceApplyLineDiff(currentAmount, invoiceApplyLine);

            BigDecimal sumTotalAmount = header.getTotalAmount().add(invoiceApplyLine.getTotalAmount());
            BigDecimal sumTaxAmount = header.getTaxAmount().add(invoiceApplyLine.getTaxAmount());
            BigDecimal sumExcludeTaxAmount = header.getExcludeTaxAmount().add(invoiceApplyLine.getExcludeTaxAmount());

            BigDecimal totalDiff = sumTotalAmount.add(diff.getTotalAmount());
            BigDecimal taxDiff = sumTaxAmount.add(diff.getTaxAmount());
            BigDecimal excludeTaxAmountDiff = sumExcludeTaxAmount.add(diff.getExcludeTaxAmount());

            header.setTotalAmount(totalDiff);
            header.setTaxAmount(taxDiff);
            header.setExcludeTaxAmount(excludeTaxAmountDiff);
        });

        invoiceApplyHeader.setTotalAmount(header.getTotalAmount());
        invoiceApplyHeader.setTaxAmount(header.getTaxAmount());
        invoiceApplyHeader.setExcludeTaxAmount(header.getExcludeTaxAmount());
    }

    /**
     * calculate the difference of total, tax, and exclude tax amounts between two invoice lines
     * @param current invoice line to be updated
     * @param prev    previous line from the database
     * @return new invoice line with the difference between current and prev 3 amounts
     */
    public static InvoiceApplyLine invoiceApplyLineDiff(InvoiceApplyLine current, InvoiceApplyLine prev) {

        BigDecimal totalAmountDiff = current.getTotalAmount().subtract(prev.getTotalAmount());
        BigDecimal taxAmountDiff = current.getTaxAmount().subtract(prev.getTaxAmount());
        BigDecimal excludeTaxAmountDiff = current.getExcludeTaxAmount().subtract(prev.getExcludeTaxAmount());

        InvoiceApplyLine lineDiff = new InvoiceApplyLine();
        lineDiff.setTotalAmount(totalAmountDiff);
        lineDiff.setExcludeTaxAmount(excludeTaxAmountDiff);
        lineDiff.setTaxAmount(taxAmountDiff);

        return lineDiff;
    }

    /**
     * calculate the total, tax, and exclude tax amount from the invoice line object
     * @param invoiceApplyLine invoice line object
     * @return new object with calculated amount
     */
    public static InvoiceApplyLine calculateAmountLine(InvoiceApplyLine invoiceApplyLine) {
        BigDecimal lineTotalAmount = invoiceApplyLine.getUnitPrice().multiply(invoiceApplyLine.getQuantity());
        BigDecimal lineTaxAmount = lineTotalAmount.multiply(invoiceApplyLine.getTaxRate());
        BigDecimal lineExcludeTaxAmount = lineTotalAmount.subtract(lineTaxAmount);

        invoiceApplyLine.setTotalAmount(lineTotalAmount);
        invoiceApplyLine.setTaxAmount(lineTaxAmount);
        invoiceApplyLine.setExcludeTaxAmount(lineExcludeTaxAmount);

        return invoiceApplyLine;
    }
}

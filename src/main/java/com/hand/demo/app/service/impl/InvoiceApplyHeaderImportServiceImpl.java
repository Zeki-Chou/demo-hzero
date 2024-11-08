package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.util.InvoiceApplyHeaderUtils;
import com.hand.demo.infra.util.Utils;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ImportService(templateCode = "EXAM-47359-HEADER")
public class InvoiceApplyHeaderImportServiceImpl extends BatchImportHandler {

    private final ObjectMapper objectMapper;
    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final CodeRuleBuilder codeRuleBuilder;

    public InvoiceApplyHeaderImportServiceImpl(ObjectMapper objectMapper, InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, InvoiceApplyLineRepository invoiceApplyLineRepository, CodeRuleBuilder codeRuleBuilder) {
        this.objectMapper = objectMapper;
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
        this.codeRuleBuilder = codeRuleBuilder;
    }

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeader> headers = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            try {
                InvoiceApplyHeader header = objectMapper.readValue(data.get(i), InvoiceApplyHeader.class);
                headers.add(header);
            } catch (JsonProcessingException e) {
                getContextList().get(i).addErrorMsg("Error Reading JSON Context");
            }
        }

        // filter based on apply header numbers, not apply header id
        List<InvoiceApplyHeader> insertList = headers
                .stream()
                .filter(line -> line.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyHeader> updateList = headers
                .stream()
                .filter(line -> line.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());

        insertList.forEach(header -> {
            String headerNumber = InvoiceApplyHeaderUtils.generateTemplateCode(codeRuleBuilder);
            header.setApplyHeaderNumber(headerNumber);
            header.setDelFlag(0);
            header.setTotalAmount(BigDecimal.ZERO);
            header.setTaxAmount(BigDecimal.ZERO);
            header.setExcludeTaxAmount(BigDecimal.ZERO);
        });

        updateList.forEach(line -> {
            // find apply header id to use the batch update by primary key
            InvoiceApplyHeader headerRecord = new InvoiceApplyHeader();
            headerRecord.setApplyHeaderNumber(line.getApplyHeaderNumber());
            InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectOne(headerRecord);

            line.setObjectVersionNumber(header.getObjectVersionNumber());
            line.setApplyHeaderId(header.getApplyHeaderId());

            InvoiceApplyLine invoiceApplyLineRecord = new InvoiceApplyLine();
            invoiceApplyLineRecord.setApplyHeaderId(line.getApplyHeaderId());
            List<InvoiceApplyLine> applyLineFromDb = invoiceApplyLineRepository.selectList(invoiceApplyLineRecord);

            // empty list since we didn't include invoice lines for the import
            List<InvoiceApplyLine> applyLines = new ArrayList<>();
            // add amount from invoice lines and add it to the real header
            Utils.addAmountFromLineList(applyLines, line, applyLineFromDb);
        });

        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        return Boolean.TRUE;
    }
}

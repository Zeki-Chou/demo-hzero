package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47357-HEADER", sheetName = "INV_APPLY_HEADER")
})
@Service
public class InvoiceImportHeaderValidatorServiceImpl extends BatchValidatorHandler {

    private final InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public InvoiceImportHeaderValidatorServiceImpl(InvoiceApplyHeaderRepository invoiceApplyHeaderRepository, ObjectMapper objectMapper) {
        this.invoiceApplyHeaderRepository = invoiceApplyHeaderRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean validate(List<String> data) {
        // Check if data is empty
        if (data.isEmpty()) {
            return false;
        }

        // Iterate over each JSON string in the data list
        for (int i = 0; i < data.size(); i++) {
            String jsonData = data.get(i);
            try {
                // Deserialize JSON to InvoiceApplyHeader object
                InvoiceApplyHeader invoiceApplyHeader = objectMapper.readValue(jsonData, InvoiceApplyHeader.class);

                // Extract applyHeaderNumber from the deserialized data
                String applyHeaderNumber = invoiceApplyHeader.getApplyHeaderNumber();

                // Check if applyHeaderNumber is provided in data
                if (applyHeaderNumber != null && !applyHeaderNumber.isEmpty()) {
                    // Build the condition to query the InvoiceApplyHeader entity
                    Condition condition = new Condition(InvoiceApplyHeader.class);
                    condition.createCriteria().andEqualTo("applyHeaderNumber", applyHeaderNumber);

                    // Query the database with the condition
                    List<InvoiceApplyHeader> headers = invoiceApplyHeaderRepository.selectByCondition(condition);

                    // Check if the applyHeaderNumber exists in the database
                    if (!headers.isEmpty()) {
                        // applyHeaderNumber exists in the database and aligns, return true for valid entry
                        getContext().get(i).addBackInfo("Data " + (i + 1) + ": Ready to update data.");
                    } else {
                        // applyHeaderNumber does not match the database, set error and return false
                        getContext().get(i).addErrorMsg("Data " + (i + 1) + ": The inputted ApplyHeaderNumber doesn't match any records in the database.");
                        return false;
                    }

                } else {
                    // applyHeaderNumber is not provided in data and does not exist in database, return true
                    getContext().get(i).addBackInfo("Data " + (i + 1) + ": Ready to insert data.");
                }

            } catch (Exception e) {
                // Handle any exception (e.g., malformed JSON)
                e.printStackTrace();
                getContext().get(i).addErrorMsg("Data " + (i + 1) + ": Error processing the row.");
                return false;
            }
        }

        // Return true if all entries pass validation logic
        return true;
    }
}

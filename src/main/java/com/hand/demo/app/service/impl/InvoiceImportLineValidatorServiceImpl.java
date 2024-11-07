package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.hzero.mybatis.domian.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@ImportValidators({
        @ImportValidator(templateCode = "EXAM-47357-LINE", sheetName = "INV_APPLY_LINE")
})
@Service
public class InvoiceImportLineValidatorServiceImpl extends BatchValidatorHandler {

    private final InvoiceApplyLineRepository invoiceApplyLineRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public InvoiceImportLineValidatorServiceImpl(InvoiceApplyLineRepository invoiceApplyLineRepository, ObjectMapper objectMapper) {
        this.invoiceApplyLineRepository = invoiceApplyLineRepository;
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
                // Deserialize JSON to InvoiceApplyLine object
                InvoiceApplyLine invoiceApplyLine = objectMapper.readValue(jsonData, InvoiceApplyLine.class);

                // Extract applyLineId from the deserialized data
                Long applyLineId = invoiceApplyLine.getApplyLineId();

                // Check if applyLineId is provided in data
                if (applyLineId != null) {

                    Condition condition = new Condition(InvoiceApplyLine.class);
                    condition.createCriteria().andEqualTo("applyLineId", applyLineId);

                    // Query the database with the condition
                    List<InvoiceApplyLine> lines = invoiceApplyLineRepository.selectByCondition(condition);

                    // Check if the applyLineId exists in the database
                    if (!lines.isEmpty()) {
                        getContext().get(i).addBackInfo("Data " + (i + 1) + ": Ready to update data.");
                    } else {
                        getContext().get(i).addErrorMsg("Data " + (i + 1) + ": The inputted ApplyLineId doesn't match any records in the database.");
                        return false;
                    }

                } else {
                    // applyLineId is not provided in data and does not exist in database, return true
                    getContext().get(i).addBackInfo("Data " + (i + 1) + ": Ready to insert data.");
                }

            } catch (Exception e) {
                // Handle any exception
                e.printStackTrace();
                getContext().get(i).addErrorMsg("Data " + (i + 1) + ": Error processing the row.");
                return false;
            }
        }

        // Return true if all entries pass validation logic
        return true;
    }
}

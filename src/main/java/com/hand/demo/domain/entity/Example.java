package com.hand.demo.domain.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import lombok.Getter;
import lombok.Setter;
import org.hzero.core.util.Regexs;

/**
 * 实体
 */
@ModifyAudit
@VersionAudit
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "example")
@Getter
@Setter
public class Example extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";


    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "error.code.null")
    @Pattern(regexp = Regexs.CODE, message = "error.code.illegal")
    private String code;

    @NotNull(message = "error.name.null")
    private String name;

}

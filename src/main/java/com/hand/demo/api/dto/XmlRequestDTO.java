package com.hand.demo.api.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Add", namespace = "http://tempuri.org/")
@XmlType(propOrder = { "intA", "intB" })
public class XmlRequestDTO {

    private String intA;
    private String intB;

    @XmlElement(name = "intA", namespace = "http://tempuri.org/")
    public String getIntA() {
        return intA;
    }

    public void setIntA(String intA) {
        this.intA = intA;
    }

    @XmlElement(name = "intB", namespace = "http://tempuri.org/")
    public String getIntB() {
        return intB;
    }

    public void setIntB(String intB) {
        this.intB = intB;
    }
}
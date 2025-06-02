package com.cakequake.cakequakeback.member.dto.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessVerificationRequestWrapper {

    @JsonProperty("businesses")
    private List<BusinessVerificationRequestDTO> businesses;
}

package com.cakequake.cakequakeback.member.dto.admin;

import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingSellerPageRequestDTO extends PageRequestDTO {

    private String status;
}

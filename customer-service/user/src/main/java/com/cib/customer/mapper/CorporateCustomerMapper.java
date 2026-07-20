package com.cib.customer.mapper;


import com.cib.customer.dto.CorporateCustomerRequest;
import com.cib.customer.dto.CorporateCustomerResponse;
import com.cib.customer.entity.CorporateCustomer;
import com.cib.customer.enums.Status;
public class CorporateCustomerMapper {

        private CorporateCustomerMapper() {
        }

        public static CorporateCustomer toEntity(CorporateCustomerRequest request) {

            return CorporateCustomer.builder()
                    .companyName(request.getCompanyName())
                    .companyCode(request.getCompanyCode())
                    .address(request.getAddress())
                    .status(Status.ACTIVE)
                    .build();
        }

        public static CorporateCustomerResponse toResponse(CorporateCustomer customer) {

            return CorporateCustomerResponse.builder()
                    .id(customer.getId())
                    .companyName(customer.getCompanyName())
                    .companyCode(customer.getCompanyCode())
                    .address(customer.getAddress())
                    .status(customer.getStatus())
                    .build();
        }
    }

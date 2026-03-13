package com.uplus.crm.domain.extraction.dto.request;

public record ExcellentCaseSearchRequest(
	    String status,
	    String sortBy, 
	    String direction 
	) {}
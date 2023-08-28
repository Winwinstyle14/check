package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "contract_original_link")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ContractOriginalLink extends Base implements Serializable{

	@Column
    @Length(max = 10)
    private String code;
	
	@Column(name = "original_link")
    @Length(max = 100)
    private String originalLink;
	
	@Column(name = "exprire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date exprireTime;

}

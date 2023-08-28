package com.vhc.ec.contract.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "ceca_log")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class CeCALog extends Base implements Serializable { 
	@Column(name="contract_id")
    private int contractId;
	
    @Column
    private int status; 

    @Column
    private String message;
    
    @Column(name="send_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate;
    
    @Column(name="sender_id")
    private String senderId;
    
    @Column(name="message_id")
    private String messageId;
    
    @Column(name="transaction_id")
    private String transactionId;
    
    @Column(name="reference_message_id")
    private String referenceMessageId;
}
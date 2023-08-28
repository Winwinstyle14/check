package com.vhc.ec.notification.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Message extends Base implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private int type;

    @Column
    private String code;

    @Column(name = "mail_template")
    private String mailTemplate;

    @Column(name = "sms_template")
    private String smsTemplate;

    @Column(name = "notice_template")
    private String noticeTemplate;

    @Column
    private String url;
}

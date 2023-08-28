package com.vhc.ec.contract.entity;

import com.vhc.ec.contract.converter.ShareTypeConverter;
import com.vhc.ec.contract.definition.ShareType;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "shares")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Share extends Base implements Serializable {

    @Column
    private String email;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date startAt;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt;

    @Column
    @Convert(converter = ShareTypeConverter.class)
    private ShareType shareType;

    @Column
    private String token;

    @Column
    private int contractId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private transient Contract contract;
}

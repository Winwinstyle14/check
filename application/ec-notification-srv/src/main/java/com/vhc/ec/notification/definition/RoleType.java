package com.vhc.ec.notification.definition;

/**
 * @author Galaxy
 * @version 1.0
 * @since 1.0
 * @Decription 
 * coordinates: Điều phối 
 * <br>
 * secretary: Văn thư 
 * <br>
 * consider: Xem xét 
 * <br>
 * signatures: Ký 
 * 
 *  + contract-signature => c
	+ signatures => s9
	+ consider => c9
	+ secretary => s8
	+ coordinates => c8
 */
public enum RoleType {
	c8,//coordinates,
	s8,//secretary,
	c9,//consider,
	s9//signatures
}

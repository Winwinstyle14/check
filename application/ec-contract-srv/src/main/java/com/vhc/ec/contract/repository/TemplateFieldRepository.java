package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TemplateFieldRepository extends JpaRepository<TemplateField, Integer> {

    Collection<TemplateField> findByContractIdOrderByCoordinateYAsc(int contractId);

    Collection<TemplateField> findAllByRecipientId(int recipientId);

    long deleteByContractId(int contractId);

    void deleteByRecipientId(int recipientId);
}

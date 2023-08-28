package com.vhc.ec.contract.dto;

import com.vhc.ec.contract.definition.FieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyProcessResField {
        private int id;

        private FieldType type;

        private short page;

        private int documentId;
}

package com.dcs.gmall.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oracle.jrockit.jfr.StringConstantPool;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GmallException extends RuntimeException {

    private String message;
    private Integer code;

}

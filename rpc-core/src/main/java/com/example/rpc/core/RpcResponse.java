package com.example.rpc.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter
@Setter
@ToString
public class RpcResponse implements Serializable {
    private String requestId;
    private Object result;
    private Exception exception;

    private int statusCode;
    private String errorMessage;
}
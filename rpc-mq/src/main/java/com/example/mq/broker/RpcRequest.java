package com.example.mq.broker;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter
@Setter
@ToString
public class RpcRequest implements Serializable {
    private String requestId;
    private String serviceName;
    private String methodName;
    private String topic;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

}
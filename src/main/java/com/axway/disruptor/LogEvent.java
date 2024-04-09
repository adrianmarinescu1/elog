package com.axway.disruptor;

public class LogEvent
{
    private String value;

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}

package com.axway.disruptor;

import com.lmax.disruptor.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogEventHandler implements EventHandler<LogEvent>
{
    private Logger httpElog = LogManager.getLogger("httpElog");

    private int count = 0;
    private int batchSize = 100;
    private StringBuilder sb = new StringBuilder();

    public LogEventHandler(int batchSize){
        this.batchSize = batchSize;
    }

    public void onEvent(LogEvent event, long sequence, boolean endOfBatch)
    {
        sb.append(event.getValue());

        if(++count > batchSize){
            httpElog.info(sb.toString());
            sb = new StringBuilder();
        }
    }
}
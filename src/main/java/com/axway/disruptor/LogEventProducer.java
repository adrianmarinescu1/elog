package com.axway.disruptor;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class LogEventProducer
{
    private final RingBuffer<LogEvent> ringBuffer;

    public LogEventProducer(RingBuffer<LogEvent> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb)
    {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try
        {
            LogEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            event.setValue(StandardCharsets.UTF_8.decode(bb).toString());  // Fill with data
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }
}
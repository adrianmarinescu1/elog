package com.axway.disruptor;


import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;


import java.nio.ByteBuffer;
import java.util.concurrent.Executors;


public class DisruptorLogger {

    private int bufferSize = 1024;
    private int batchSize = 100;
    private LogEventFactory factory = new LogEventFactory();
    private Disruptor<LogEvent> disruptor;
    private long howLong = 0;

    public DisruptorLogger(int bufferSize, int batchSize){
        this.bufferSize = bufferSize;
        this.batchSize = batchSize;
        String configBufferSize = System.getProperty("log4j2.asyncLoggerRingBufferSize");
        if(configBufferSize != null)
            bufferSize = Integer.parseInt(configBufferSize);

        disruptor = new Disruptor<>(factory, bufferSize, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(new LogEventHandler(batchSize));

    }

    public void start(){
        disruptor.start();
    }


    public void logEvent(String message){
        RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
        LogEventProducer producer = new LogEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(message.length());
        long start = System.currentTimeMillis();
        producer.onData(bb);
        long end = System.currentTimeMillis();
        howLong += end - start;
    }

    public void shutdown(){
        disruptor.shutdown();
        System.out.println("Disruptor inner time: " + howLong);
    }
}

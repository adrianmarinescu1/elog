package com.axway.fltest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Random;

import com.axway.disruptor.DisruptorLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.AsyncLogger;

public class FileLockTest {

    private Logger simpleAsyncElog = LogManager.getLogger("simpleAsyncElog");
    private Logger fullAsyncElog = LogManager.getLogger("fullAsyncElog");
    private Logger httpAsyncElog = LogManager.getLogger("httpAsyncElog");
    private DisruptorLogger disruptorLogger;


    private int howManyWrites = 0;
    private String prefix = "prefix";
    private long howLong = 0;
    private int mod = 1;

    public FileLockTest(int howManyWrites, String prefix, int mod){
        this.howManyWrites = howManyWrites;
        this.prefix = prefix;
        this.mod = mod;

        disruptorLogger = new DisruptorLogger( 1024, mod);
    }

    private FileChannel createFile(String name) throws IOException{
        FileChannel channel = null;
        File file = new File(name);
        file.createNewFile();
        RandomAccessFile rad = new RandomAccessFile(file, "rw");
        rad.setLength(0);
        channel = rad.getChannel();
        return channel;
    }

    public static int getRandomNumberInts(int min, int max){
        Random random = new Random();
        return random.ints(min,(max+1)).findFirst().getAsInt();
    }

    private void testLog(){
        FileLock lock = null;
        FileChannel channel = null;
        StringBuilder sb = new StringBuilder();
        howLong = 0;
        try {
            channel = createFile("log.log");

            for(int i = 1; i < howManyWrites; i++)
            {
                sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
                ByteBuffer bf = ByteBuffer.wrap(sb.toString().getBytes());
                if(i % mod == 0){

                    long start = System.currentTimeMillis();
                    lock = channel.lock();
                    channel.write(bf);
                    lock.release();
                    long end = System.currentTimeMillis();
                    howLong += end - start;
                    sb = new StringBuilder();
                }
            }
            System.out.println("TimeTaken lockfile: " + howLong);
            // Close the file
            channel.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if(lock != null) {
                try{
                    lock.release();
                } catch(IOException ioe){
                }
            }
            if(channel != null){
                try{
                    channel.close();
                } catch(IOException ioe){
                }
            }
        }

    }


    private void testSimpleAsync(){

        howLong = 0;
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < howManyWrites; i++)
        {
            sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
            if(i % mod == 0){
                long start = System.currentTimeMillis();
                simpleAsyncElog.info(sb.toString());
                long end = System.currentTimeMillis();
                howLong += end - start;
                sb = new StringBuilder();
            }
        }
        System.out.println("TimeTaken SimpleAsync: " + howLong);

    }

    private void testFullAsync(){

        howLong = 0;
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < howManyWrites; i++)
        {
            sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
            if(i % mod == 0){
                long start = System.currentTimeMillis();
                fullAsyncElog.info(sb.toString());
                long end = System.currentTimeMillis();
                howLong += end - start;
                sb = new StringBuilder();
            }
        }
        System.out.println("TimeTaken FullAsync: " + howLong);

    }

//    private void testSocketAppender(){
//        howLong = 0;
//        StringBuilder sb = new StringBuilder();
//        for(int i = 1; i < howManyWrites; i++)
//        {
//            sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
//            if(i % mod == 0){
//                long start = System.currentTimeMillis();
//                elogSocket.info(sb.toString());
//                long end = System.currentTimeMillis();
//                howLong += end - start;
//                sb = new StringBuilder();
//            }
//        }
//        System.out.println("TimeTaken Log4JSocket: " + howLong);
//
//    }

    private void testHttpAppender(){
        howLong = 0;
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < howManyWrites; i++)
        {
            sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
            if(i % mod == 0){
                long start = System.currentTimeMillis();
                httpAsyncElog.info(sb.toString());
                long end = System.currentTimeMillis();
                howLong += end - start;
                sb = new StringBuilder();
            }
        }
        System.out.println("TimeTaken Log4JHttp: " + howLong);

    }

    private void testDisruptorLogger(){
        howLong = 0;
        disruptorLogger.start();
        for(int i = 1; i < howManyWrites; i++)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append("Message").append(i).append(System.getProperty("line.separator"));
            long start = System.currentTimeMillis();
            disruptorLogger.logEvent(sb.toString());
            long end = System.currentTimeMillis();
            howLong += end - start;
        }
        System.out.println("TimeTaken DisruptorLogger: " + howLong);
        disruptorLogger.shutdown();
    }

    public static void main(String[] args){
        int howManyWrites = Integer.parseInt(args[0] == null ? "0" : args[0]);
        String prefix = args[1] == null ? "prefix" : args[1];
        int mod = Integer.parseInt(args[2] == null ? "1" : args[2]);

        FileLockTest flt = new FileLockTest(howManyWrites, prefix, mod);
        flt.testLog();
        System.out.println("---");
        flt.testSimpleAsync();
        System.out.println("---");
        flt.testFullAsync();
        System.out.println("---");
        flt.testHttpAppender();
        System.out.println("---");
        flt.testDisruptorLogger();
        //AsyncLogger

    }
}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread " + t.getName(), e);
        try(FileWriter fw = new FileWriter("log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(e + " at Thread: " + t.getName());
            for (StackTraceElement s : e.getStackTrace()){
                out.println(s);
            }
            //more code
        } catch (IOException ae) {
            //exception handling left as an exercise for the reader
        }
    }
}
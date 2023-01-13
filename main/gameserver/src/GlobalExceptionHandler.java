import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static  final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread " + t.getName(), e);
        try(FileWriter fw = new FileWriter("log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("####################### " + dateFormat.format(System.currentTimeMillis()) + " #######################");
            out.println();
            out.println(e + " at Thread: " + t.getName());
            for (StackTraceElement s : e.getStackTrace()){
                out.println(s);
            }
            out.println();

        } catch (IOException ae) {
            ae.printStackTrace();
        }
    }
}
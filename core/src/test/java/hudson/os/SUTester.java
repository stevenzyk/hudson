package hudson.os;

import hudson.remoting.Callable;
import hudson.util.StreamTaskListener;

import java.io.FileOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class SUTester {
    public static void main(String[] args) throws Throwable {
        SU.execute(new StreamTaskListener(System.out),"kohsuke","bogus",new Callable<Object, Throwable>() {
            public Object call() throws Throwable {
                System.out.println("Touching /tmp/x");
                new FileOutputStream("/tmp/x").close();
                return null;
            }
        });
    }
}

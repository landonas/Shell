/**
 * Created by landonsoriano on 10/1/15.
 */

import java.io.*;

public class PipeThread extends Thread {

    InputStream in;
    OutputStream out;
    private boolean b;

    public PipeThread(InputStream in, OutputStream out, boolean b) {
        this.in = in;
        this.out = out;
        this.b = b;
    }

    public void run() {
        int x;

        try {
            while ((x = in.read()) != -1) {
                out.write(x);
            }

        } catch (IOException e) {
            System.out.println("ERRORR" + e);
        }

        if(b){
            closeOutputStream();
        }
    }
    public void closeOutputStream()
    {
        try {
            this.out.close();
        } catch (IOException e) {
            System.err.println("I/O Error");
        }
    }

}


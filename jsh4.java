/**
 * Created by landonsoriano on 9/30/15.
 */
import java.util.*;
import java.io.*;
public class jsh4{

    public static void main(String[] args) throws Exception {
        jsh4 shell = new jsh4();
        shell.run();
    }

    public jsh4() {

    }

    public void run() throws Exception {
        // Variable declarations
        String homeDir = System.getProperty("user.dir");
        String currDir = homeDir;
        String myCommand = " ";
        ArrayList<String> commandList = new ArrayList<String>();
        ArrayList<PipeThread> outList = new ArrayList<PipeThread>();
        ArrayList<PipeThread> errList = new ArrayList<PipeThread>();
        Scanner scan = new Scanner(System.in);



        // Will terminate when it meets EOF on standard input, i.e., when the
        // user types ^c.
        while (!myCommand.equals("c")) {
            String myCommand2 = " ";
            System.out.print("Jsh1>");

            try {
                myCommand = scan.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("^C");
                System.exit(0);

            }

			/*
			 * Pipe method
			 *
			 */

            myCommand = myCommand.trim();
            String[] tokens = myCommand.split("[ \t\n]+");

            // in case no command is entered
            if (myCommand.equals("")) {
                continue;
            }

            // if the inputted commands contain a pipe character
            if (myCommand.indexOf("|") != -1 ) {
                // copies myCommand to a string
                String first = myCommand;
                String second = null;
                // splits the string
                String[] firstToken = first.split("\\s+");
                String[] secondToken = first.split("\\s+");

                // to split command, we split first thing into a substring from
                // first char to the pipe "|"
                String firstArg = first.substring(0, first.indexOf("|"));
                //System.out.print(firstArg);
                // then for the second arg we go from were the pipe ended + 1,
                // to the length of the string command.
                String secondArg = first.substring(first.indexOf("|") + 1, first.length());
                //System.out.print(secondArg);

                String firstA = firstArg.trim();
                String secondA = secondArg.trim();

                // set these strings to an array
                String[] newArray1 = firstA.split("\\s+");
                String[] newArray2 = secondA.split("\\s+");

                // process list
                Process processOne = null;
                Process processTwo = null;



                ProcessBuilder pb1 = new ProcessBuilder(newArray1); // process builder for first
                ProcessBuilder pb2 = new ProcessBuilder(newArray2);// process builder for second

                try {
                    processOne = pb1.start();
                    processTwo = pb2.start();
                } catch (IOException e) {
                    throw new Exception("Invald.");
                }

                //processOne = pb1.start();
                //processTwo = pb2.start();

                // streams for processes
                InputStream stderr1 = processOne.getErrorStream();
                InputStream stdout1 = processOne.getInputStream();
                InputStream stderr2 = processTwo.getErrorStream();
                InputStream stdout2 = processTwo.getInputStream();
                OutputStream stdin2 = processTwo.getOutputStream();
                OutputStream stdin1 = processOne.getOutputStream();

                // setup pipes wrong

				/*
				 * I think I did that part within my PipeThread class right? I will have to pass that boolean onto indicate if it should be open or no though.

					Yup. Your PipeThread class looks good. And remember that you should never close System.out and System.err.

					Im not sure what you mean launch another pipeThread, something like this? :
					PipeThread out0 = new PipeThread(processOne.getInputStream(),processTwo.getOutputStream(), true);

 					Yes, this is okay but you also need one that reads from processTwo and prints to System.out.
 				*/


                //only need 4
                //p1 > p2 > sys out
                //p2 > p
                PipeThread p1out = new PipeThread(stdout1, stdin2, true);
                PipeThread p1error = new PipeThread(stderr1, System.err, false);
                PipeThread p2Out = new PipeThread(stdout2,System.out,false);
                PipeThread p2Err = new PipeThread(stderr2,System.err,false);



                //start all pipes
                p1out.start();
                p1error.start();
                p2Out.start();
                p2Err.start();





                //wait for the last process to finish

                processTwo.waitFor();



                //is this the right spot for the break you mentioned after the final?
                continue;

            }// ends pipe

            // command for path
            if (myCommand.equals("pwd")) {
                System.out.println(currDir);
                continue;
            }

            // command for jobs
            if (myCommand.equals("jobs")) {
                for (int z = 0; z < commandList.size(); z++) {
                    System.out.println(z + " " + commandList.get(z));
                }
                continue;
            }

            // command for fg
            if (tokens[0].equals("fg")) {
                if (tokens.length < 2) {
                    continue;
                }

                int index = Integer.parseInt(tokens[1]);

                try {
                    outList.get(index).join();
                    errList.get(index).join();

                } catch (Exception e) {
                    System.out.println("Invalid");
                    continue;
                }

                // sets list err and out
                commandList.set(index, commandList.get(index) + " [FINISHED]");
                errList.set(index, null);
                outList.set(index, null);
                continue;
            }



            // command for cd
            if (tokens[0].equals("cd")) {
                String tempDir = homeDir;
                // if there are more than 2 arguments syntax error
                if (tokens.length > 2) {
                    System.out.println("Syntax error.");
                    continue;
                }

                // if just cd, go to homedir
                if (tokens.length == 1) {
                    currDir = homeDir;
                }

                else {
                    // absolute path
                    if (tokens[1].charAt(0) == '/') {
                        tempDir = tokens[1];
                        File ab = new File(tempDir);

                        if (ab.exists()) {
                            currDir = tempDir;
                        }

                        else {
                            System.out.println("Invalid directory.");
                            continue;
                        }

                    }// ends else for absolute path

                    // relative pathing, process 1 by 1
                    else {
                        String[] cdTokens = tokens[1].split("/");
                        // System.out.println(cdTokens[0]);

                        for (int x = 0; x < cdTokens.length; x += 1) {

                            if (cdTokens[x].equals("..")) {

                                int last = currDir.lastIndexOf("/");

                                if (last > 1) {
                                    tempDir = currDir.substring(0, last);
                                    File rlt = new File(tempDir);

                                    if (rlt.exists()) {
                                        currDir = tempDir;
                                        continue;
                                    }

                                    else {
                                        System.out
                                                .println("Invalid directory.");
                                        continue;
                                    }

                                }

                                else {
                                    tempDir = "/";
                                    File rlt3 = new File(tempDir);

                                    if (rlt3.exists()) {
                                        currDir = tempDir;
                                        continue;
                                    }

                                    else {
                                        System.out
                                                .println("Invalid directory.");
                                        continue;
                                    }

                                }

                            }// ends if

                            else {
                                if (currDir.length() > 1) {
                                    tempDir = currDir + "/" + cdTokens[x];
                                    File rlt2 = new File(tempDir);
                                    if (rlt2.exists()) {
                                        currDir = tempDir;
                                    }

                                    else {
                                        System.out
                                                .println("Invalid directory.");
                                        continue;
                                    }
                                }// ends if for length of current directory

                                else {
                                    tempDir = currDir + cdTokens[x];
                                    File rlt5 = new File(tempDir);
                                    if (rlt5.exists()) {
                                        currDir = tempDir;
                                    }

                                    else {
                                        System.out
                                                .println("Invalid directory.");
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }

            }

            //make sure "if process" doesnt go here?
            else {
                // ProcessBuilder is an object that knows how to create
                // external processes like "ls -u"
                ProcessBuilder peanutButter2 = null;
                Process jelly2 = null;

                // declaring variables for newTok
                String[] newTok = new String[tokens.length + 1];
                String[] newTok2 = new String[tokens.length];
                String[] newTok3 = new String[tokens.length - 1];

                if (tokens[tokens.length - 1].equals("&")) {

                    for (int y = 0; y < tokens.length - 1; y += 1) {
                        newTok2[y] = tokens[y];
                        newTok3[y] = tokens[y];
                    }

                    newTok2[newTok2.length - 1] = currDir;

                    if (!tokens[0].equals("sleep") && !tokens[0].equals("ps")) {

                        peanutButter2 = new ProcessBuilder(newTok2);
                    }

                    else {
                        peanutButter2 = new ProcessBuilder(newTok3);
                    }
                }

                else {

                    for (int y = 0; y < tokens.length; y += 1) {
                        newTok[y] = tokens[y];
                    }

                    newTok[newTok.length - 1] = currDir;

                    if (!tokens[0].equals("sleep") && !tokens[0].equals("ps")) {
                        peanutButter2 = new ProcessBuilder(newTok);
                    }

                    else {
                        peanutButter2 = new ProcessBuilder(tokens);
                    }
                }

                try {
                    jelly2 = peanutButter2.start();

                } catch (IOException e)

                {
                    System.err.println("Unknown command \'" + myCommand
                            + "\'\n");

                    continue;
                }


                // Input/Output relative to this process
                // getErrorStream() gives an InputStream because the current
                // process reads input from the created process's stderr
                // getInputStream() gives an InputStream because the current
                // process reads input from the created process's stdout
                // getOutputStream() gives an OutputStream because the current
                // process writes output into the created process's stdin
                // System.err.println("Establishing streams to communicate with process...");

                InputStream sstderr = jelly2.getErrorStream();
                InputStream sstdout = jelly2.getInputStream();

                // Typical to create BufferReader objects associated to all
                // above streams

                PipeThread pipe = new PipeThread(sstderr, System.out, false);
                PipeThread pipe2 = new PipeThread(sstdout, System.out, false);

                (pipe2).start();
                (pipe).start();

                if (tokens[tokens.length - 1].equals("&")) {
                    commandList.add(myCommand);
                    outList.add(pipe2);
                    errList.add(pipe);
                    //counter += 1;
                } else {
                    try {
                        (pipe2).join();
                        (pipe).join();

                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e + "....");
                    }
                }
            }
        }

        scan.close();

    }
}


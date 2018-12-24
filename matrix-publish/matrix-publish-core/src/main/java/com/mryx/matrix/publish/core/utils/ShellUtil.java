package com.mryx.matrix.publish.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.List;

/**
 * Created by dinglu on 2018/9/12.
 */
public class ShellUtil {

    private static final Logger logger = LoggerFactory.getLogger(ShellUtil.class);

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    private ShellUtil() {
        throw new AssertionError();
    }

    /**
     * check whether has root permission
     *
     * @return
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    /**
     * execute shell command, default return result msg
     *
     * @param command command
     * @param isRoot  whether need to run with root
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command list
     * @param isRoot   whether need to run with root
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command array
     * @param isRoot   whether need to run with root
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /**
     * execute shell command
     *
     * @param command         command
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     * <p>
     * <p>
     * 最基础方法
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
     * {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some excepiton.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader succesult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();


            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                succesult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = succesult.readLine()) != null) {
                    successMsg.append(s).append("\n");
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s).append("\n");
                }
            }

            result = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (succesult != null) {
                    succesult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }

        logger.info("result:" + result);
        if (successMsg != null) {
            logger.info("successMsg:" + successMsg.toString());
        }
        if (errorMsg != null) {
            logger.info("errorMsg:" + errorMsg.toString());
        }

        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
     */
    public static class CommandResult {

        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        public String successMsg;
        /**
         * error message of command result
         **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }


    public static void main(String[] args) throws Exception {

        //

        //String[] cmd = new String[]{"ps aux", "pwd", "ls -al"};
        //执行shell命令
        //execCommand(cmd, false, true);
        //执行shell脚本

        String GIT_ADDRESS="git@git.missfresh.cn:grampus/grampus-bi-api.git";
        String GIT_NAME="grampus-bi-api/";
        String script = "sh /Users/dinglu/gitclone.sh " +GIT_NAME+ " " +GIT_ADDRESS;


        exeCmd(script);

//使用ProcessBuilder
//    exeCmd("ps aux;ls -al;pwd");//执行linux命令
//    exeCmd("dir&dir");//执行windows命令一行多命令用&分隔

    }


//使用ProcessBuilder


    /**
     * @param shell shell可以传入多个命令  ,例如:"ps aux;ls -al;pwd"
     * @return
     * @throws IOException
     * @author yuan hai 2016年12月9日
     */
    public static int exeCmd(String shell) throws IOException {
        int success = 0;
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;

        // get name representing the running Java virtual machine.
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];

        try {
            System.out.println("Starting to exec{ " + shell + " }. PID is: " + pid);
            Process process = null;

            //执行脚本时，不要加-c
            // 当执行系统的命令时，才会加-c
            //linux命令行
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", shell);
            //windows命令行
            //  ProcessBuilder pb = new ProcessBuilder("cmd", "/c", shell);

            pb.environment();
            pb.redirectErrorStream(true); // merge error stream into standard stream
            process = pb.start();
            if (process != null) {
                br = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "GBK"), 1024);

            } else {
                System.out.println("There is no PID found.");
            }
            sb.append("Ending exec right now, the result is：\n");
            String line = null;
            while (br != null && (line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            //记得在start()之后， waitFor（）之前把缓冲区读出来打log
            process.waitFor();
        } catch (Exception ioe) {
            sb.append("Error occured when exec cmd：\n").append(ioe.getMessage()).append("\n");
        } finally {
            PrintWriter writer = null;
            if (br != null) {
                br.close();
            }
            try {
                writer = new PrintWriter(System.out);
                writer.write(sb.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                writer.close();
            }
            success = 1;
        }
        return success;
    }

    public static Object exec(String cmd) {
        try {
            String[] cmdA = { "/bin/sh", "-c", cmd };
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

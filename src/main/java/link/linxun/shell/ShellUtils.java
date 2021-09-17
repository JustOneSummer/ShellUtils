package link.linxun.shell;

import link.linxun.shell.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * shell执行工具类
 *
 * @author LinXun
 * @date 2021/2/2 14:43 星期二
 */
public class ShellUtils {
    private static final Logger log = LoggerFactory.getLogger(ShellUtils.class);
    private static final Pattern PATTERN_WINDOWS = Pattern.compile("\\d(.*)");

    private ShellUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体 多条命令可用 && 拆分
     */
    public static void call(String cmd) {
        call(cmd, getCharset());
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体-自动拆分
     */
    public static void call(List<String> cmd) {
        call(cmd, getCharset());
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体 多条命令可用 && 拆分
     */
    public static void call(String cmd, Charset charset) {
        try {
            if (Platform.isWindows()) {
                cmd = "cmd /c " + cmd;
            }
            log.debug("shell exec coding={}", charset);
            Process process = Runtime.getRuntime().exec(cmd);
            // 获取返回信息的流
            print(process.getInputStream(), "info", charset);
            print(process.getErrorStream(), "error", charset);
        } catch (IOException e) {
            log.debug("shell exec error command={}", cmd, e);
        }
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体
     */
    public static void call(List<String> cmd, Charset charset) {
        StringBuilder cs = new StringBuilder();
        try {
            log.debug("shell exec coding={}", charset);
            if (Platform.isWindows()) {
                cs.append("cmd /c ");
                cmd.forEach(c -> {
                    cs.append(c);
                    cs.append(" && ");
                });
                cs.delete(cs.length() - 4, cs.length() - 1);
                exec(cs.toString(), charset);
            } else {
                for (String c : cmd) {
                    exec(c, charset);
                }
            }
        } catch (IOException e) {
            log.debug("shell exec error command={}", cmd, e);
        }
    }

    /**
     * 执行
     *
     * @param cmd     命令
     * @param charset 编码
     * @throws IOException 异常
     */
    private static void exec(String cmd, Charset charset) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        // 获取返回信息的流
        print(process.getInputStream(), "info", charset);
        print(process.getErrorStream(), "error", charset);
    }

    /**
     * 输出shell 信息
     *
     * @param in      输入流
     * @param level   日志等级
     * @param charset 输出编码
     * @throws IOException e
     */
    private static void print(InputStream in, String level, Charset charset) throws IOException {
        Reader reader = new InputStreamReader(in, charset);
        BufferedReader bReader = new BufferedReader(reader);
        String res = bReader.readLine();
        while (res != null) {
            log.debug("shell {} === >>> {}", level, res);
            res = bReader.readLine();
        }
        bReader.close();
        reader.close();
    }

    /**
     * 输出shell 信息
     *
     * @param in 输入流
     * @return 信息(默认UTF - 8 编码)
     * @throws IOException e
     */
    private static String print(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bReader = new BufferedReader(reader);
        String res = bReader.readLine();
        StringBuilder buffer = new StringBuilder();
        while (res != null) {
            buffer.append(res);
            res = bReader.readLine();
        }
        bReader.close();
        reader.close();
        return buffer.toString();
    }

    /**
     * 获取系统编码
     *
     * @return 编码
     */
    private static Charset getCharset() {
        if (Platform.isWindows()) {
            try {
                //解析cmd当前的编码
                Process process = Runtime.getRuntime().exec("cmd /c chcp");
                String print = print(process.getInputStream());
                log.debug("shell exec system code={}", print);
                Matcher matcher = PATTERN_WINDOWS.matcher(print);
                if (matcher.find()) {
                    switch (matcher.group()) {
                        case "936":
                            return Charset.forName("GBK");
                        case "437":
                            return StandardCharsets.ISO_8859_1;
                        case "65001":
                        default:
                            return StandardCharsets.UTF_8;
                    }
                }
            } catch (IOException e) {
                log.error("get charset error ", e);
            }
        }
        return StandardCharsets.UTF_8;
    }
}

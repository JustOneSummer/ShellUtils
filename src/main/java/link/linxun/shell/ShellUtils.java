package link.linxun.shell;

import link.linxun.shell.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LinXun
 * @date 2021/2/2 14:43 星期二
 */
public class ShellUtils {
    private static final Logger log = LoggerFactory.getLogger(ShellUtils.class);
    private static final Pattern PATTERN_WINDOWS = Pattern.compile("\\d(.*)");
    private static final Pattern PATTERN_LINUX = Pattern.compile("LC_MESSAGES=(.*)");

    private ShellUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体 多条命令可用 && 拆分
     */
    public static void call(String cmd) {
        try {
            if (Platform.isWindows()) {
                cmd = "cmd /c " + cmd;
            }
            Charset charset = getCharset();
            log.debug("shell exec 当前编码={}", charset);
            Process process = Runtime.getRuntime().exec(cmd);
            // 获取返回信息的流
            print(process.getInputStream(), "info", charset);
            print(process.getErrorStream(), "error", charset);
        } catch (IOException e) {
            log.debug("shell exec error 命令={}", cmd, e);
        }
    }

    /**
     * 发送命令
     *
     * @param cmd 指令体
     */
    public static void call(List<String> cmd) {
        List<String> cs = new ArrayList<>();
        try {
            if (Platform.isWindows()) {
                cs.add("cmd /c ");
            }
            cmd.forEach(c -> {
                cs.add(" &&");
                cs.add(c);
            });
            Charset charset = getCharset();
            log.debug("shell exec 当前编码={}", charset);
            Process process = Runtime.getRuntime().exec(cs.toArray(new String[0]));
            // 获取返回信息的流
            print(process.getInputStream(), "info", charset);
            print(process.getErrorStream(), "error", charset);
        } catch (IOException e) {
            log.debug("shell exec error 命令={}", cmd, e);
        }
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
     * @throws IOException io异常
     */
    private static Charset getCharset() throws IOException {
        if (Platform.isWindows()) {
            //解析cmd当前的编码
            Process process = Runtime.getRuntime().exec("cmd /c chcp");
            String print = print(process.getInputStream());
            log.debug("shell exec 系统编码信息={}", print);
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
        } else if (Platform.isLinux()) {
            //解析系统当前的编码
            Process process = Runtime.getRuntime().exec("locale");
            String print = print(process.getInputStream());
            log.debug("shell exec 系统编码信息={}", print);
            Matcher matcher = PATTERN_LINUX.matcher(print);
            if (matcher.find()) {
                return Charset.forName(matcher.group().split("\\.")[1].replace("\"", ""));
            }
        }
        return StandardCharsets.UTF_8;
    }
}

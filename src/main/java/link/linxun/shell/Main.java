package link.linxun.shell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinXun
 * @date 2021/2/4 14:03 星期四
 */
public class Main {
    public static void main(String[] args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("sudo cd /etc");
        cmd.add("sudo cd /web");
        cmd.add("sudo ls -l");
        ShellUtils.call(cmd);
        System.out.println("=======================");
        ShellUtils.call("sudo cd /etc &&  sudo cd /web && sudo ls -l");
    }
}

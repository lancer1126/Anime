package org.lance.utils;

import java.net.ServerSocket;
import java.util.Locale;

/**
 * 操作系统相关工具类
 * @author lancer1126
 */
public class OSUtil {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    /**
     * 判断端口是否已被使用
     */
    public static boolean checkIsPortsUsing(int... ports) {
        boolean isPortsUsing = false;
        ServerSocket serverSocket = null;
        for (int port : ports) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (Exception e) {
                isPortsUsing = true;
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return isPortsUsing;
    }
}

package org.lance.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

@Slf4j
public class FFmpegUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegUtil.class);

    private static final String FFMPEG_PATH = "D:\\CodeSpace\\GitHub\\Java\\AnimeDownloader\\ffmpeg.exe";

    public static boolean run(String command) {
        Process process = null;
        InputStream errorStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec(command);
            errorStream = process.getErrorStream();
            inputStreamReader = new InputStreamReader(errorStream);
            br = new BufferedReader(inputStreamReader);

            String str = "";
            while ((str = br.readLine()) != null) {
                LOGGER.debug(str);
            }
            process.waitFor();
            process.destroy();
            process.destroyForcibly();
            process.exitValue();
            System.out.println("process 执行完毕");
            return true;
        } catch (Exception e) {
            LOGGER.error(e.toString());
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (errorStream != null) {
                try {
                    errorStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public static boolean convert(String filePath, String videoFilePath, String audioFilePath) {
        String command = createConvertCmd(filePath, videoFilePath, audioFilePath);
        File mp4File = new File(filePath);
        if (!mp4File.exists()) {
            LOGGER.info("The download is complete, and the merging video {} and audio {} ...", videoFilePath, audioFilePath);
            run(command);
            if (mp4File.exists()) {
                LOGGER.info("merge finish");
                return true;
            }
        } else {
            LOGGER.info("skip merge");
            return true;
        }
        return false;
    }

    public static String createConvertCmd(String filePath, String videoFilePath, String audioFilePath) {
        String command = FFMPEG_PATH + " -i " + videoFilePath + " -i " + audioFilePath
                + " -c copy " + filePath;
        LOGGER.info(command);
        return command;
    }
}

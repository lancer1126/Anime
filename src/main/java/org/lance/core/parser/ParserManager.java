package org.lance.core.parser;

import lombok.extern.slf4j.Slf4j;
import org.lance.common.AnimeException;
import org.lance.common.annotation.Parser;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * ParserManager
 * @author lancer1126
 */
@Slf4j
public class ParserManager {

    private static final String PARSER_PACKAGE_NAME = "org.lance.core.parser";

    private List<AbstractParser> parserList = new ArrayList<>();

    public static ParserManager getInstance() {
        // 静态内部类创建单例
        return ParserManagerHolder.INSTANCE;
    }

    public void init() {
        log.info("加载解析器");
        Reflections reflections = new Reflections(PARSER_PACKAGE_NAME);
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Parser.class);
        List<Class<?>> refClassList = new ArrayList<>(classSet);

        List<AbstractParser> tempParserList = new ArrayList<>();
        refClassList.stream()
                .sorted(Comparator.comparingInt(e -> e.getAnnotation(Parser.class).weight()))
                .forEach(e -> {
                    Object o = null;
                    try {
                        o = e.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                    }
                    tempParserList.add((AbstractParser) o);
                });
        parserList = tempParserList;
        log.info("解析器加载完毕");
    }

    public TaskInfo buildTaskInfo(RequestHeader requestHeader, VideoInfo videoInfo) throws AnimeException {
        for (AbstractParser parser : parserList) {
            if (parser.matchParser(videoInfo.getType())) {
                return parser.buildTaskInfo(requestHeader, videoInfo);
            }
        }
        return null;
    }

    private static class ParserManagerHolder {
        private static final ParserManager INSTANCE = new ParserManager();
    }
}

package regex;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.*;

/**
 * 只是返回字符串,还需要后续的转换成对象 如 LocalDateTime
 * */
public class RegexTools {
    private ArrayList<String> regexResults ;
    private String timeFormatter;
    private ArrayList<String> timeComponent;
    public RegexTools(){
        /**
         * arraylist 一定要初始化哦  要不会空指针呢
         * */
        regexResults = new ArrayList<>();
        //这个 pattern 是规定的 我就写在这了 共 6 个 groups
        this.timeFormatter = "(\\d{4})/(\\d{1,2})/(\\d{1,2}).+?(\\d+):(\\d)+:(\\d+)";
    }

    /**
     * 核心方法
     *
     * */
    public ArrayList<String> doRegex(String targetStr, String patternStr) {
        //we use Regex to find our the targetStr we want
        //case_insensitive and dotall mode
        Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        //Pattern pattern = Pattern.compile(patternStr);
        //matcher str
        Matcher matcher = pattern.matcher(targetStr);

        //注意 就是 这个 ArrayList 并不是用来保存同一种信息,只是拿来保存可能存在的多个 group 而已
        //group(0) 为整个匹配字符串哦 我们不使用的
        boolean isFind = matcher.find();
        //System.out.println("group count is " + matcher.groupCount());
        if (isFind) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                regexResults.add(matcher.group(i));
            }
        }
        //System.out.println(regexResults);
        //finished , return
        return regexResults;

    }

    /**
     *
     * 这个不同签名的方法 专门用于 转换字符串为日期的
     * */
    public LocalDateTime doRegex (String timeStr){

        //这里其实可以添加一个判断字符串是否为 规定格式的 检查,这里暂时不写

        timeComponent = doRegex( timeStr ,timeFormatter);
        //[发布日期:2017/11/1 10:23:59] -> time
        return LocalDateTime.of(parseInt(timeComponent.get(0)),
                                parseInt(timeComponent.get(1)),
                                parseInt(timeComponent.get(2)),
                                parseInt(timeComponent.get(3)),
                                parseInt(timeComponent.get(4)),
                                parseInt(timeComponent.get(5))
                                );
    }

    }


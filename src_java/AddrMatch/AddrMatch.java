package AddrMatch;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Desc:地址匹配度算法
 * @Author: WenRj
 * @Date: 2019/7/24
 */

public class AddrMatch {
    private final static List<String> SPLIT_LIST =
            Arrays.asList("省","市","县","镇","街道","村","自然村","小区","社区","区","里","弄","塘","乡");
    private final static double DECAY_VALUE = 0.3; //衰减值

    public static void main(String[] args) {
        String addr = "浙江省长兴县雉城街道申兴小区8-2-202至";
        String group = "长兴县雉城镇长兴小额零散客户群";
        Map<String, Object> resultMap = addrSimilarity(addr, group);
        System.out.println(resultMap.get("matchValue"));
        System.out.println(resultMap.get("matchWords"));
    }

    /**
     * @Desc:字符串拼音
     * @Author: WenRj
     * @param:
     * @return:
     * @Date: 2019/7/24
     */
    public static String characterToPinyin(char hanyu) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        String[] pinyinArray = null;
        pinyinArray = PinyinHelper.toHanyuPinyinStringArray(hanyu);

        if (pinyinArray == null)
            return null;

        return pinyinArray[0].substring(0, pinyinArray[0].length() - 1);
    }

    /**
     * @Desc:将字符串转为拼音
     * @Author: WenRj
     * @param:
     * @return:
     * @Date: 2019/7/24
     */
    public static String strToPinyin(String hanyu) {
        StringBuilder sb = new StringBuilder();
        String tempPinyin = null;

        for (int i = 0; i < hanyu.length(); i++) {
            tempPinyin = characterToPinyin(hanyu.charAt(i));
            if (tempPinyin == null) {
                sb.append(hanyu.charAt(i));
            } else {
                sb.append(tempPinyin);
            }
        }

        return sb.toString();
    }

    /**
     * @Desc: 去重
     * @Author: WenRj
     * @param:
     * @return:
     * @Date: 2019/7/24
     */
    public static ArrayList<String> removeDuplicate(ArrayList<String> arrayList) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < arrayList.size(); ++i){
            if (!list.contains(arrayList.get(i))){
                list.add(arrayList.get(i));
            }
        }
        return list;

    }

    /**
     * @Desc: 正则匹配，去掉非汉字的字符，‘群’
     * @Author: WenRj
     * @param:
     * @return:
     * @Date: 2019/7/24
     */
    public static boolean isHaveHanzi(String str) {
        String reg = "[a-zA-Z0-9群]+";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(str);
        return mat.find();
    }


    /**
     * @Desc:将汉字字符串转为拼音
     * @Author: WenRj
     * @param:
     * @return:
     * @Date: 2019/7/24
     */
    public static List<String> splitToPinyin(String str) {
        List<String> addrlist = new ArrayList<String>();
        Pattern p = Pattern.compile("[" + SPLIT_LIST +"]");
        String[] strs = p.split(str);
        ArrayList<String> strList = new ArrayList<String>(Arrays.asList(strs));
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < SPLIT_LIST.size(); ++i) {
            if (strList.get(strList.size() - 1).equals(SPLIT_LIST.get(i))) {
                strList.remove(strList.size() - 1);
                break;
            }
        }
        strList = removeDuplicate(strList);
        for (int i = 0; i < strList.size(); ++i) {
            if (!isHaveHanzi(strList.get(i))) {
                arrayList.add(strList.get(i));
            }
        }
        for (int i = 0; i < arrayList.size(); ++i) {
            String pinyin = "";
            pinyin = strToPinyin(arrayList.get(i));
            addrlist.add(pinyin);
        }
        return addrlist;
    }

    /**
     * @Desc: 地址相识度计算
     * @Author: WenRj
     * @param: addr:
     * @return: matchWords:匹配值; matchValue :匹配度;
     * @Date: 2019/7/24
     */
    public static Map<String, Object> addrSimilarity(String addr, String group) {
        addr = addr.trim();
        group = group.trim();
        List<String> addrlist = new ArrayList<String>();
        List<String> grouplist = new ArrayList<String>();
        addrlist = splitToPinyin(addr);
        grouplist = splitToPinyin(group);

        double total_sum = 0.0;
        double match_sum = 0.0;
        int mul_index = 0;
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<String> matchList = new ArrayList<String>();
        //匹配度算法
        for (int i = grouplist.size() - 1; i >= 0; --i){
            String cell = grouplist.get(i);
            total_sum += Math.pow(DECAY_VALUE, mul_index);
            if (addrlist.contains(cell)) {
                matchList.add(cell);
                match_sum += Math.pow(DECAY_VALUE, mul_index);
            }
            mul_index++;
        }
        if (total_sum == 0) {
            return null;
        }
        resultMap.put("matchWords", matchList);
        resultMap.put("matchValue", match_sum / total_sum);
        return resultMap;
    }

}

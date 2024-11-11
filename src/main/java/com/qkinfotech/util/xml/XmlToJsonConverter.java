package com.qkinfotech.util.xml;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 组件XML数据转JSON工具类
 */
@Slf4j
public class XmlToJsonConverter {

    //XML文件头
    private static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";


    public static void main(String[] args) throws Exception {
        String xml = "<dataList><data id=\"192939587e51214ecf65b3345e2960fb\" text=\"评标专家和评标专家库管理办法\" catehref=\"/km/institution/?categoryId=171a2ea54c141e3e2a3be5b4ce491183\" created=\"2025-01-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=192939587e51214ecf65b3345e2960fb\" catename=\"国家发展和改革委员会\" creator=\"杨浩\" /><data id=\"191de851faf577649f1d21c42bf8160c\" text=\"法规规章备案审查条例\" catehref=\"/km/institution/?categoryId=171a2e9e6d16575a9c7a9c8424d8401e\" created=\"2024-11-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=191de851faf577649f1d21c42bf8160c\" catename=\"国务院\" creator=\"杨浩\" /><data id=\"1926f18e1cb0bb3cfc666cf4ebc8c10e\" text=\"上海市建设工程造价咨询标准\" catehref=\"/km/institution/?categoryId=171862b2beaa2292ad59317403f9170e\" created=\"2024-10-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=1926f18e1cb0bb3cfc666cf4ebc8c10e\" catename=\"上海市住建委\" creator=\"杨浩\" /><data id=\"191de59bba3b4824ff9053d402ba134c\" text=\"关于支持首台（套）重大技术装备平等参与企业招标投标活动的指导意见\" catehref=\"/km/institution/?categoryId=171a5038fdaa94b1719c0414b36b5f2d\" created=\"2024-09-11\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=191de59bba3b4824ff9053d402ba134c\" catename=\"工业和信息化部\" creator=\"杨浩\" /><data id=\"19080404c774aa1230298b44c7084189\" text=\"公平竞争审查条例\" catehref=\"/km/institution/?categoryId=171a2e9e6d16575a9c7a9c8424d8401e\" created=\"2024-08-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=19080404c774aa1230298b44c7084189\" catename=\"国务院\" creator=\"杨浩\" /><data id=\"19122151aae87ddb487c28a4b74b7667\" text=\"关于规范中央企业采购管理工作的指导意见\" catehref=\"/km/institution/?categoryId=1912214047a365d4952a44a4952b4497\" created=\"2024-07-18\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=19122151aae87ddb487c28a4b74b7667\" catename=\"国资委\" creator=\"杨浩\" /><data id=\"190b5486a92ac0b454efa3d48d2a987c\" text=\"中华人民共和国公司法\" catehref=\"/km/institution/?categoryId=171a2e92f272b45ce0fbd944b15a1785\" created=\"2024-07-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=190b5486a92ac0b454efa3d48d2a987c\" catename=\"全国人民代表大会常务委员会\" creator=\"杨浩\" /><data id=\"1908043fb12f89e0207f63549f084a21\" text=\"国务院关于实施《中华人民共和国公司法》注册资本登记管理制度的规定\" catehref=\"/km/institution/?categoryId=171a2e9e6d16575a9c7a9c8424d8401e\" created=\"2024-07-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=1908043fb12f89e0207f63549f084a21\" catename=\"国务院\" creator=\"杨浩\" /><data id=\"18df44f1fa808b1e863264f4149a8eb3\" text=\"上海市在沪工程监理企业信用评价管理办法\" catehref=\"/km/institution/?categoryId=171862b2beaa2292ad59317403f9170e\" created=\"2024-07-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=18df44f1fa808b1e863264f4149a8eb3\" catename=\"上海市住建委\" creator=\"杨浩\" /><data id=\"18df445610128917618b3f6414fbf5cc\" text=\"上海市在沪建筑业企业信用评价管理办法\" catehref=\"/km/institution/?categoryId=171862b2beaa2292ad59317403f9170e\" created=\"2024-07-01\" href=\"/km/institution/km_institution_knowledge/kmInstitutionKnowledge.do?method=view&amp;fdId=18df445610128917618b3f6414fbf5cc\" catename=\"上海市住建委\" creator=\"杨浩\" /></dataList>";

        System.out.println(getXmlToJsoArray(xml));
    }



/*    public static void main(String[] args) {
        String info = "<data id=\"1916a7591d8c0a71cda32b84a6ab889e\" text=\"供应商领购\" catehref=\"/sys/news/sys_news_main/sysNewsMain.do?method=view&fdId=1916a739a8781b1d08f4d214a6ca90ae\" href=\"/km/professional/km_professional_sign/kmProfessionalSign.do?method=view&fdId=1916a7591d8c0a71cda32b84a6ab889e&fdNewId=1916a739a8781b1d08f4d214a6ca90ae\" otherinfo=\"&nbsp<a target='_blank' href='/ekp/km/professional/km_professional_sign/kmProfessionalSign.do?method=view&fdId=1916a7591d8c0a71cda32b84a6ab889e' title='点击后可快速进入查看页面!请在2024-08-20 00:00:00之前完成领购流程!'><font color='#4285F4'><b>【已审核】</b></font></a>&nbsp<font color='#4285F4'><b>【已支付】</b></font></a>&nbsp<a target='_blank' href='/ekp/km/professional/km_professional_info/kmProfessionalInfo.do?method=download&fdNewId=1916a739a8781b1d08f4d214a6ca90ae' title='点击后可快速下载标书!'><font color='red'><b>【下载标书】</b></font></a>&nbsp<a target='_blank' href='/ekp/km/professional/km_professional_info/kmProfessionalInfo.do?method=add&fdSignId=1916a7591d8c0a71cda32b84a6ab889e' title='点击后可快速上传标书!请在2024-08-21 00:00:00之前完成标书上传!'><font color='red'><b>【待上传】<sup>(0)</sup></b></font></a>\" catename=\"0402053002\" creator=\"上海恩拿马生物科技有限公司\"/>";
        // 正则表达式匹配所有的 <a> 标签及其内容
        // 注意：这个正则表达式假设<a>标签是良好格式化的，并且没有嵌套的<a>标签
        String regex = "<a[^>]*>([\\s\\S]*?)</a>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);

        List<String> linksContent = new ArrayList<>(); // 用于存储<a>标签内容的列表

        // 找到所有匹配的 <a> 标签并提取内容
        while (matcher.find()) {
            // 将匹配到的 <a> 标签内容添加到列表中
            linksContent.add(matcher.group(0));
        }

        // 打印所有找到的<a>标签内容
        for (String content : linksContent) {
            System.out.println(content);
        }
    }*/

    /**
     * xml单行解析器
     *
     * @param xml    xml数据
     * @param fields xml中出现的固定字段
     * @return 转换为json的字符串
     */
    public static JSONObject getXmlToJson(String xml, String[] fields) {
        //默认将双引号转换为单引号
        xml = xml.replace('"', '\'');
        JSONObject jsonObject = new JSONObject();

        for (String field : fields) {
                if ("otherinfo".equals(field)) {
                xml = xml.replace("&lt;","<").replace("&gt;",">");
                String regex = "<a[^>]*>([\\s\\S]*?)</a>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(xml);
                List<String> linksContent = new ArrayList<>(); // 用于存储<a>标签内容的列表
                // 找到所有匹配的 <a> 标签并提取内容
                while (matcher.find()) {
                    // 将匹配到的 <a> 标签内容添加到列表中
                    linksContent.add(matcher.group(0));
                }
                jsonObject.put(field, linksContent);
            } else {
//            int prefix = xml.indexOf(field + "='") + field.length() + 2;
                int prefixIndex = xml.indexOf(field + "='"); // 找到字段的开始位置
                if (prefixIndex == -1) {
                    jsonObject.put(field, ""); // 如果字段不存在，添加NULL
                    continue; // 继续下一个字段
                }
                int valueStartIndex = prefixIndex + field.length() + 2; // 跳过字段名和等号以及引号
                int valueEndIndex = xml.indexOf("'", valueStartIndex); // 找到字段值的结束位置
                if (valueEndIndex == -1) {
                    jsonObject.put(field, ""); // 如果没有结束引号，添加NULL
                    continue; // 继续下一个字段
                }

                String fieldValue = xml.substring(valueStartIndex, valueEndIndex);
                jsonObject.put(field, fieldValue);

                xml = xml.substring(0, prefixIndex) + xml.substring(valueEndIndex + 1);
            }
        }
        return jsonObject;
    }

    /**
     * xml多行解析器
     *
     * @param xml    xml数据
     * @return 转换为jsonArray的字符串
     */
    public static JSONArray getXmlToJsoArray(String xml) {
        JSONArray jsonArray = new JSONArray();

        if(!xml.contains("<dataList>")){
            //判断当前xml数据中是否拥有datalist标签，如果没有则为空
            return jsonArray;
        }
        //添加文件头
        xml = XML_HEAD + xml;

        try {
            Pattern pattern = Pattern.compile("<data[^>]*otherinfo=\"(.*?)\"[^>]*/>");
            Matcher matcher = pattern.matcher(xml);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                // 获取 otherinfo 的原始值
                String originalOtherinfo = matcher.group(1);
                // 替换 otherinfo 中的 < 和 >
                String replacedOtherinfo = originalOtherinfo.replace("<", "&lt;").replace(">", "&gt;");
                // 构建替换后的整个 <data ... /> 结构
                matcher.appendReplacement(result, matcher.group().replace(originalOtherinfo, replacedOtherinfo));
            }
            matcher.appendTail(result);

            //将无法识别的&转义为&amp;
            xml = result.toString().replaceAll("&","&amp;");

            // 使用dom4j解析XML
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));
            // 获取根元素
            Element rootElement = document.getRootElement();

            // 遍历所有的data元素
            for (Iterator<Element> it = rootElement.elementIterator("data"); it.hasNext();) {
                Element dataElement = it.next();

                // 创建一个JSONObject来存储当前data元素的所有属性
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < dataElement.attributeCount(); i++) {
                    if("otherinfo".equals(dataElement.attribute(i).getName())){
                        //需要将内容的&lt;和&gt;转义回来
                        String value = dataElement.attribute(i).getValue();
                        value = value.replace("&lt;","<").replace("&gt;",">");
                        jsonObject.put(dataElement.attribute(i).getName(), value);
                    }else{
                        jsonObject.put(dataElement.attribute(i).getName(), dataElement.attribute(i).getValue());
                    }

                }
                // 将JSONObject添加到JSONArray中
                jsonArray.add(jsonObject);
            }
        }catch (Exception e){
            logger.error("xml转换失败时的内容：{}", xml);
            e.printStackTrace();
        }
        return jsonArray;
    }

}

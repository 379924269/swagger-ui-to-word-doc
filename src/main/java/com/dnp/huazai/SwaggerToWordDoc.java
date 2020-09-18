package com.dnp.huazai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * description:
 *
 * @author: 华仔
 * @date: 2020/8/18
 */
public class SwaggerToWordDoc {
    static int ID = 0;

    public static void convert() {
        try {
            JSONObject jsonData = getSwaggerJsonData();
            JSONObject info = jsonData.getJSONObject("info");
            /* controller类的描述，tags是一个数组 每个数组作用有下面的字段 "name": "video-controller","description": "ptt视频采集相关信息" */
            JSONArray tags = jsonData.getJSONArray("tags");
            /* requestPathsJsonObject 请求路径和参数等 */
            JSONObject paths = jsonData.getJSONObject("paths");
            /* 定义的一些实体类返回参数 */
            JSONObject definitions = jsonData.getJSONObject("definitions");

            StringBuilder htmlBuilder = new StringBuilder();
            generateHtml(info, tags, paths, definitions, htmlBuilder);

            IOUtils.write(htmlBuilder.toString().getBytes(), new BufferedOutputStream(new FileOutputStream(new File("SwaggerData.html"))));
            IOUtils.write(htmlBuilder.toString().getBytes(), new BufferedOutputStream(new FileOutputStream(new File("SwaggerData.doc"))));

            System.out.println("generateHtmlBuilder = " + htmlBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateHtml(JSONObject info, JSONArray tags, JSONObject paths, JSONObject definitions, StringBuilder htmlBuilder) {
        createHtmlHead(info, htmlBuilder);

        for (int i = 0; i < tags.size(); i++) {
            createSecondaryTitle(tags, htmlBuilder, i);

            String targController = tags.getJSONObject(i).getString("name");
            for (String path : paths.keySet()) {
                JSONObject pathMethods = paths.getJSONObject(path);

                for (String pathMethod : pathMethods.keySet()) {
                    JSONObject methodData = paths.getJSONObject(path).getJSONObject(pathMethod);
                    String methodDataSummary = methodData.getString("summary");
                    String methodDataController = methodData.getJSONArray("tags").get(0).toString();
                    if (targController.equalsIgnoreCase(methodDataController)) {
                        generateTable(definitions, htmlBuilder, path, methodDataSummary, methodData, pathMethod);
                    }
                }
            }
        }

        htmlBuilder.append("</body>\n</html>");
    }

    private static void createSecondaryTitle(JSONArray tags, StringBuilder htmlBuilder, int i) {
        String title = tags.getJSONObject(i).getString("description");
        htmlBuilder.append("<h2>" + title + "</h2>");
    }

    private static void generateTable(JSONObject definitions, StringBuilder htmlBuilder,
                                      String keyName, String methodSummary, JSONObject methodData, String method) {
        htmlBuilder.append("<h3>" + methodSummary + "</h3>" +
                "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" width = \"80%\">" +
                "   <tr style=\"background: rgb(215, 215, 215)\">\n" +
                "       <td colspan=\"4\">" + methodSummary + "</td>\n" +
                "   </tr>" +
                "   <tr>\n" +
                "       <td style=\"background: rgb(215, 215, 215)\">url(" + method + ")</td>\n" +
                "       <td colspan=\"3\">" + keyName + "\n</td>\n" +
                "   </tr>" +
                "   <tr style=\"background: rgb(215, 215, 215)\">\n" +
                "       <td>字段名称</td>\n" +
                "       <td>字段类型</td>\n" +
                "       <td>是否必须</td>\n" +
                "       <td>字段描述</td>\n" +
                "   </tr>");
        JSONArray paramsArray = methodData.getJSONArray("parameters");
        if (paramsArray != null) {
            for (int j = 0; j < paramsArray.size(); j++) {
                JSONObject paramObj = paramsArray.getJSONObject(j);
                htmlBuilder.append("<tr>\n" +
                        "<td>" + paramObj.getString("name") + "</td>\n" +
                        "<td>" + paramObj.getString("type") + "</td>\n" +
                        "<td>" + paramObj.getString("required") + "</td>\n" +
                        "<td>" + paramObj.getString("description") + "</td>\n" +
                        "</tr>");
            }
        } else {
            htmlBuilder.append("<tr>\n" +
                    "   <td>无</td>\n" +
                    "   <td>无</td>\n" +
                    "   <td>无</td>\n" +
                    "   <td>无</td>\n" +
                    "</tr>");
        }

        htmlBuilder.append("<tr style=\"background: rgb(215, 215, 215)\">\n" +
                "   <td>返回状态</td>\n" +
                "   <td colspan=\"3\">返回结果</td>\n" +
                "</tr>");

        JSONObject response = methodData.getJSONObject("responses");
        dealResponse(definitions, htmlBuilder, response);

        htmlBuilder.append("</table>");
    }

    private static void dealResponse(JSONObject definitions, StringBuilder htmlBuilder, JSONObject response) {
        for (String ok200 : response.keySet()) {
            JSONObject ok200Obj = response.getJSONObject(ok200);

            if (!ok200Obj.containsKey("schema")) {
                generateResultRow(ok200 + "  //" + ok200Obj.getString("description"), "无", htmlBuilder);
            } else {
                for (String resultKey : ok200Obj.keySet()) {
                    if (resultKey.equals("schema")) {
                        String ref = null;
                        if (ok200Obj.getJSONObject(resultKey).containsKey("ref")) {
                            ref = ok200Obj.getJSONObject(resultKey).getString("ref");
                        } else {
                            if (ok200Obj.getJSONObject(resultKey).containsKey("items")) {
                                ref = ok200Obj.getJSONObject(resultKey).getJSONObject("items").getString("ref");
                            }
                        }

                        if (ref != null) {
                            generateResultRow(ok200, getRefValue(definitions, ref).toString(), htmlBuilder);
                        } else {
                            generateResultRow(ok200, new JSONObject().toString(), htmlBuilder);
                        }
                    }
                }
            }
        }
    }

    private static JSONObject getSwaggerJsonData() throws IOException {
        String jsonDataPath = System.getProperty("user.dir") + "\\src\\main\\resources\\SwaggerData.json";
        String xx = JSON.toJSON(IOUtils.toString(new FileReader(jsonDataPath)).replace("$", "")).toString();
        return JSONObject.parseObject(xx);
    }

    private static void createHtmlHead(JSONObject info, StringBuilder htmlBuilder) {
        String docTitle = info.getString("title");
        String head =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "   <meta charset=\"UTF-8\">\n" +
                        "   <title>Title</title>" +
                        "       <script type=\"text/javascript\">\n" +
                        "           window.onload = function(){\n" +
                        "               for (let i = 1; i < 1000; i++) {\n" +
                        "                   var id = \"show_json\" + i;\n" +
                        "                   const text = document.getElementById(id).innerText;\n" +
                        "                   if (text) {\n" +
                        "                     try {\n" +
                        "                        const result = JSON.stringify(JSON.parse(text), null, 100);\n" +
                        "                        document.getElementById(id).innerHTML = \"<pre>\" + result + \"</pre>\";\n" +
                        "                    }catch (e) {\n" +
                        "                        console.log(e);\n" +
                        "                    }" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "       </script>" +
                        "       <style>\n" +
                        "           table{border-right:1px black;border-bottom:1px black}\n" +
                        "           table td{border-left:1px black;border-top:1px black}\n" +
                        "       </style>" +
                        "       </head>\n" +
                        "<body>" +
                        "<h1 align=\"center\">" + docTitle + "</h1>";
        htmlBuilder.append(head);
    }

    /**
     * description: 生成结果table
     *
     * @param resStatus : 返回结果status
     * @param resResult : 返回结果描述
     * @return : java.lang.String
     */
    private static void generateResultRow(String resStatus, String resResult, StringBuilder htmlBuilder) {
        ID++;
        String resultHtmlRow = "<tr>\n" +
                "   <td>" + resStatus + "</td>\n" +
                "       <td colspan=\"3\">" +
                "       <p id=\"show_json" + ID + "\">" +
                            resResult +
                "       </p>" +
                    "</td>\n" +
                "</tr>";
        htmlBuilder.append(resultHtmlRow);
    }

    /**
     * description: 获取自定义实体类中字段信息，封装袋json中，包含：字段：类型 //描述  生成对象如：
     * {"total":"integer","rows":{"id":"integer //streams表的id","userName":"string //用户名称","userId":"string //用户id"}}
     *
     * @param definitions : 自定义的是些实体类返回对象
     * @param ref         : 引用的实体类对象
     * @return : com.alibaba.fastjson.JSONObject json：{字段：类型 //描述 }
     */
    private static JSONObject getRefValue(JSONObject definitions, String ref) {
        JSONObject refValues = new JSONObject();
        String definitionsObject = ref.substring(ref.lastIndexOf("/") + 1);
        JSONObject properties = definitions.getJSONObject(definitionsObject).getJSONObject("properties");

        for (String propertiesKeyName : properties.keySet()) {
            JSONObject propertiesValue = properties.getJSONObject(propertiesKeyName);
            if (propertiesValue.containsKey("ref")) {
                String rowsRef = propertiesValue.getString("ref");
                refValues.put(propertiesKeyName, getRefValue(definitions, rowsRef));
            } else if (propertiesValue.containsKey("items") && propertiesValue.getJSONObject("items").containsKey("ref")) {
                boolean isArray = properties.getJSONObject(propertiesKeyName).getString("type").equals("array");
                if (isArray) {
                    String rowsRef = propertiesValue.getJSONObject("items").getString("ref");
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(getRefValue(definitions, rowsRef));
                    refValues.put(propertiesKeyName, jsonArray);
                } else {
                    String rowsRef = propertiesValue.getString("ref");
                    refValues.put(propertiesKeyName, getRefValue(definitions, rowsRef));
                }
            } else {
                refValues.put(propertiesKeyName, properties.getJSONObject(propertiesKeyName)
                        .getString("type") + "  //" + properties.getJSONObject(propertiesKeyName).getString("description"));
            }
        }
        return refValues;
    }
}

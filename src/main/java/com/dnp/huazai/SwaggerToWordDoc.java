package com.dnp.huazai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author huazai
 * @description swagger-ui转doc文档， 通过api/doc获取json数据，然后通过json数据转换为html table，然后保存为.doc格式,注意：
 * 1、fastjson 解析带上了$符号的key解析有问题
 * @date 2020/6/22
 */
public class SwaggerToWordDoc {
    public static void convert() {
        try {
            JSONObject jsonObject = getSwaggerJsonData();

            JSONObject infoObject = jsonObject.getJSONObject("info");
            /* controller类的描述，tags是一个数组 每个数组作用有下面的字段 "name": "video-controller","description": "ptt视频采集相关信息" */
            JSONArray tags = jsonObject.getJSONArray("tags");
            /* requestPathsJsonObject 请求路径和参数等 */
            JSONObject pathsJsonObject = jsonObject.getJSONObject("paths");
            /* 定义的一些实体类返回参数 */
            JSONObject definitions = jsonObject.getJSONObject("definitions");

            StringBuilder generateHtmlBuilder = new StringBuilder();

            generateHtmlBuilder.append(createHtmlHead(infoObject.getString("title")));

            int tagsLength = tags.size();
            for (int i = 0; i < tagsLength; i++) {
                generateHtmlBuilder.append("<h2>").append(tags.getJSONObject(i).getString("description")).append("</h2>");

                String targName = tags.getJSONObject(i).getString("name");
                for (String keyName : pathsJsonObject.keySet()) {
                    boolean isGet = pathsJsonObject.getJSONObject(keyName).containsKey("get");
                    boolean isPost = pathsJsonObject.getJSONObject(keyName).containsKey("post");
                    boolean isPut = pathsJsonObject.getJSONObject(keyName).containsKey("put");
                    boolean isDelete = pathsJsonObject.getJSONObject(keyName).containsKey("delete");
                    if (isGet) {
                        JSONObject getRequestMehtodJsonObject = pathsJsonObject.getJSONObject(keyName).getJSONObject("get");
                        String getPiOperationValue = getRequestMehtodJsonObject.getString("summary");
                        if (targName.equalsIgnoreCase(getRequestMehtodJsonObject.getJSONArray("tags").get(0).toString())) {
                            generateTable(definitions, generateHtmlBuilder, keyName, getPiOperationValue, getRequestMehtodJsonObject);
                            generateHtmlBuilder.append("</table>");
                        }
                    }
                    if (isPost) {
                        JSONObject postrequestMehtodJsonObject = pathsJsonObject.getJSONObject(keyName).getJSONObject("post");
                        String postapiOperationValue = postrequestMehtodJsonObject.getString("summary");
                        if (targName.equalsIgnoreCase(postrequestMehtodJsonObject.getJSONArray("tags").get(0).toString())) {
                            generateTable(definitions, generateHtmlBuilder, keyName, postapiOperationValue, postrequestMehtodJsonObject);
                            generateHtmlBuilder.append("</table>");
                        }

                    }
                    if (isPut) {
                        JSONObject putrequestMehtodJsonObject = pathsJsonObject.getJSONObject(keyName).getJSONObject("put");
                        String putapiOperationValue = putrequestMehtodJsonObject.getString("summary");
                        if (targName.equalsIgnoreCase(putrequestMehtodJsonObject.getJSONArray("tags").get(0).toString())) {
                            generateTable(definitions, generateHtmlBuilder, keyName, putapiOperationValue, putrequestMehtodJsonObject);
                            generateHtmlBuilder.append("</table>");
                        }
                    }
                    if (isDelete) {
                        JSONObject deleterequestMehtodJsonObject = pathsJsonObject.getJSONObject(keyName).getJSONObject("delete");
                        String deleteapiOperationValue = deleterequestMehtodJsonObject.getString("summary");
                        if (targName.equalsIgnoreCase(deleterequestMehtodJsonObject.getJSONArray("tags").get(0).toString())) {
                            generateTable(definitions, generateHtmlBuilder, keyName, deleteapiOperationValue, deleterequestMehtodJsonObject);
                            generateHtmlBuilder.append("</table>");
                        }
                    }

                }
            }
            generateHtmlBuilder.append("</body>\n" +
                    "</html>");
            System.out.println("generateHtmlBuilder = " + generateHtmlBuilder.toString());

            IOUtils.write(generateHtmlBuilder.toString().getBytes(), new BufferedOutputStream(new FileOutputStream(new File("SwaggerData.html"))));
            IOUtils.write(generateHtmlBuilder.toString().getBytes(), new BufferedOutputStream(new FileOutputStream(new File("SwaggerData.doc"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateTable(JSONObject definitions, StringBuilder generateHtmlBuilder, String keyName, String ApiName, JSONObject requestMehtodJsonObject) {
        generateHtmlBuilder.append("<h3>").append(ApiName).append("</h3>");

        generateHtmlBuilder.append("<table border=\"1\">");
//                        stringBuffer.append("<caption><h3 align=\"left\">" + apiOperationValue + "</h3></caption>");
        generateHtmlBuilder.append("<tr>\n" + "        <td colspan=\"4\">")
                .append(ApiName)
                .append("</td>\n    </tr>");
        generateHtmlBuilder.append("<tr>\n" + "        <td>url</td>\n" + "        <td colspan=\"3\">")
                .append(keyName)
                .append("\n        </td>\n    </tr>");

        generateHtmlBuilder.append(" <tr>\n" +
                "        <td>字段名称</td>\n" +
                "        <td>字段类型</td>\n" +
                "        <td>是否必须</td>\n" +
                "        <td>字段描述</td>\n" +
                "    </tr>");
        JSONArray requestParametersArray = requestMehtodJsonObject.getJSONArray("parameters");
        if (requestParametersArray != null) {
            for (int j = 0; j < requestParametersArray.size(); j++) {
                JSONObject requestParametersObject = requestParametersArray.getJSONObject(j);
                generateHtmlBuilder.append(" <tr>\n" + "        <td>").append(requestParametersObject.getString("name")).append("</td>\n").append("        <td>").append(requestParametersObject.getString("type")).append("</td>\n").append("        <td>").append(requestParametersObject.getString("required")).append("</td>\n").append("        <td>").append(requestParametersObject.getString("description")).append("</td>\n").append("    </tr>");
            }
        } else {
            generateHtmlBuilder.append(" <tr>\n" +
                    "        <td>无</td>\n" +
                    "        <td>无</td>\n" +
                    "        <td>无</td>\n" +
                    "        <td>无</td>\n" +
                    "    </tr>");
        }

        generateHtmlBuilder.append(" <tr>\n" +
                "        <td>返回状态</td>\n" +
                "        <td colspan=\"3\">返回结果</td>\n" +
                "    </tr>");

        JSONObject requestResponseObject = requestMehtodJsonObject.getJSONObject("responses");
        for (String responseKey : requestResponseObject.keySet()) {
            JSONObject responseObject1 = requestResponseObject.getJSONObject(responseKey);

            if (keyName.contains("comment")) {
                System.out.println(" = " + keyName);
            }

            if (!responseObject1.containsKey("schema")) {
                generateHtmlBuilder.append(returnResultInfor(responseKey + "  //" + responseObject1.getString("description"), "无"));
            } else {
                for (String resultKey : responseObject1.keySet()) {
                    if (resultKey.equals("schema")) {
                        String ref = null;
                        if (responseObject1.getJSONObject(resultKey).containsKey("ref")) {
                            ref = responseObject1.getJSONObject(resultKey).getString("ref");
                        } else {
                            System.out.println("ref = " + responseObject1);
                            if (responseObject1.getJSONObject(resultKey).containsKey("items")) {
                                ref = responseObject1.getJSONObject(resultKey).getJSONObject("items").getString("ref");
                            }
                        }

                        if (ref != null) {
                            generateHtmlBuilder.append(returnJsonResultInfor(responseKey, getRefValue(definitions, ref).toString()));
                        } else {
                            generateHtmlBuilder.append(returnJsonResultInfor(responseKey, new JSONObject().toString()));
                        }
                    }
                }
            }
        }
    }

    private static JSONObject getSwaggerJsonData() throws IOException {
        String jsonDataPath = System.getProperty("user.dir") + "\\src\\main\\resources\\SwaggerData.json";
        String xx = JSON.toJSON(IOUtils.toString(new FileReader(jsonDataPath)).replaceAll("$", "")).toString();
        return JSONObject.parseObject(xx);
    }

    private static String createHtmlHead(String docTitle) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>" +
                "<head>\n" +
                "</head>\n" +
                "<body>" +
                "<h1 align=\"center\">" + docTitle + "</h1>";
    }

    /**
     * description: 生成结果table
     *
     * @param responseKey    : 返回结果status
     * @param responseResult : 返回结果描述
     * @return : java.lang.String
     */
    private static String returnResultInfor(String responseKey, String responseResult) {
        return "<tr>\n" +
                "        <td>" + responseKey + "</td>\n" +
                "        <td colspan=\"3\">\n" +
                "            " + responseResult + "\n" +
                "        </td>\n" +
                "    </tr>";
    }

    /**
     * description: 生成结果table
     *
     * @param responseKey    : 返回结果status
     * @param responseResult : 返回结果描述
     * @return : java.lang.String
     */
    private static String returnJsonResultInfor(String responseKey, String responseResult) {
        return "<tr>\n" +
                "        <td>" + responseKey + "</td>\n" +
                "        <td colspan=\"3\">\n" +
                "        <p class=\"show_json\">" +
                "            " + responseResult + "\n" +
                "        </p>\n" +
                "        </td>\n" +
                "    </tr>";
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
        JSONObject propertiesKeyNameJsonObject = new JSONObject();
        String value = ref.substring(ref.lastIndexOf("/") + 1);
        JSONObject propertieJsonObject = definitions.getJSONObject(value).getJSONObject("properties");

        for (String propertiesKeyName : propertieJsonObject.keySet()) {
            if (propertiesKeyName.equals("rows")) {
                String rowsRef = propertieJsonObject.getJSONObject("rows").getJSONObject("items").getString("ref");
                propertiesKeyNameJsonObject.put("rows", getRefValue(definitions, rowsRef));
            } else {
                propertiesKeyNameJsonObject.put(propertiesKeyName, propertieJsonObject.getJSONObject(propertiesKeyName).getString("type") + "  //" + propertieJsonObject.getJSONObject(propertiesKeyName).getString("description"));
            }
        }
        return propertiesKeyNameJsonObject;
    }
}

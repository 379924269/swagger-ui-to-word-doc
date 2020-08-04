## swagger-ui文档转word文档
- 思路和操作：
    把swagger-ui的（api-docs接口获取）json数据放到html中的table中，然后由html转换为word文档。我的实际操作，我先是简单的写了
一个html模板（TableTest.html），然后再把数据填到html中，生成自己想要的类型，然后在结合代码，用stringbuilder生成html。具体文档
设计根据自己需要调整html，修改样式。

## swagger-ui api-doc json结构：
{
  "swagger": "2.0",      //swagger版本
  "info": {},           // 项目版本、名称、描述
  "host": "192.168.0.202:4430", 
  "basePath": "/news",
  "paths": {},
  "securityDefinitions":{},
  "definitions":{},
  "tags": []
}

## 运行
- java -jar swagger-ui-to-word-doc

运行App即可生成html和word，位置在当前项目文件夹下

### FQA
- 1.`特别注意`：在fastjson解析json数据的时候，key如果有$符号，解析有点问题，处理：$全部替换为空。 
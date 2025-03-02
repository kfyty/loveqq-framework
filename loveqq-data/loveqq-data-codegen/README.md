## loveqq-data-codegen
    代码生成器

### 生成配置
#### 新建配置文件
    固定命名为：code-generator.properties

#### 模板分组
   由于支持分组的概念，即为不同的项目编写不同的生成模板。
   因此需要在 @EnableAutoGenerate 上配置 templatePrefix。

    @EnableAutoGenerate(templatePrefix = "group1")

#### 新建文件夹
   模板分组是依赖实际文件夹层级实现的，因此需要新建文件夹，层级如下

      ${projectResourcesDir}/template/group1

#### 新建模板文件
##### 模板文件命名格式
      {类型后缀}.{文件类型后缀}.{模板文件本身后缀}

   示例1：DO.java.en

    则对于数据表 user，生成的文件为：UserDO.java，en 表示模板文件本身是 en 类型的，可随意定制

   示例2：DO_NoSu.java.en

    则对于数据表 user，生成的文件为：User.java，因为 _NoSu 表示不需要后缀
   
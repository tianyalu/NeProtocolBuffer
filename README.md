# Protocol Buffer

[TOC]

## 一、`Protocol Buffer`是什么

### 1.1 `Google`官方定义

`Protocol Buffer`是一种轻便高效的结构化数据存储格式，可适用于结构化数据序列化，适合做数据存储或者`RPC`数据交换格式，它与语言无关、平台无关，是可扩展的序列化结构数据格式。

### 1.2 优点

* 序列化后体积相比`Json`和`XML`很小，适合网络传输；
* 支持跨平台、多语言；
* 消息格式升级和兼容性还不错；
* 序列化和反序列化速度很快；

### 1.3 性能表现

| protobuf         | jackson | xstream | Serializable | hessian2 | hessian2压缩 | hessian1 |       |
| ---------------- | ------- | ------- | ------------ | -------- | ------------ | -------- | ----- |
| 序列化(单位ns)   | 1154    | 5421    | 92406        | 10189    | 26794        | 100766   | 29027 |
| 反序列化(单位ns) | 1334    | 8743    | 117329       | 64027    | 37871        | 188432   | 37596 |
| bytes            | 97      | 311     | 664          | 824      | 374          | 283      | 495   |

参考：[几种序列化协议(protobuf,xstream,jackjson,jdk,hessian)相关数据对比](https://www.iteye.com/blog/agapple-859052)

## 二、使用`Protocol Buffer`

* 环境配置：搭建`Protocol Buffer`环境；
* 构建消息：定义`.Proto`文件，编译`.Proto`文件；
* 平台应用：`Android`中使用`Protocol Buffer`.

### 2.1 环境配置（命令行）

#### 2.1.1 下载`Protocol Buffer`安装包

> 1. 官网下载最新的稳定版本；
> 2. `Protocol Buffer`有两个版本：`Proto2`、`Proto3`；
> 3. 下载地址：[https://github.com/protocolbuffers/protobuf/releases/tag/v3.9.1](https://github.com/protocolbuffers/protobuf/releases/tag/v3.9.1) .

#### 2.1.2 `Mac`下通过`Homebrew`安装

```bash
# 安装依赖
brew install autoconf automack libtool curl
# 运行自动生成脚本
cd xxx/protobuffer-3.8.1
./autogen.sh
# 运行配置脚本
./configure
# 编译为编译的依赖包
make
# 检查依赖包的完整性
make check
# 安装protocol buffer
make install

# 检查是否安装成功
protoc --version  #libprotoc 3.9.1 表示安装成功
```

#### 2.1.3 插件方式

`Google`提供了插件方式进行`protocol buffer`编译，不同版本脚本书写方式不同，这里以`3.8.0`为例.

配置应用`build.gradle`:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.google.protobuf:protobuf-gradle-plugin:0.8.10"
  }
}
```

配置模块`app`的`build.gradle`：

```groovy
apply plugin: 'com.android.application'
apply plugin: 'com.google.protobuf'

protobuf {
    protoc {
        artifact = 'com.google.protobuf:protoc:3.8.0'
    }
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option "lite"
                }
            }
        }
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
//    implementation 'com.google.protobuf:protobuf-java:3.9.1'
		//...
}
```

### 2.2 构建消息

#### 2.2.1 字段

消息至少右一个字段组合而成。

字段 = 字段修饰符 + 字段类型 + 字段名 + 标识号

> 1. 字段修饰符：设置改字段解析使 的规则（`repeated`）;
> 2. 字段类型：基本类型、枚举、消息对象；
> 3. 字段名：字段名称；
> 4. 标识号：二进制格式唯一表示每个字段。

#### 2.2.2 字段类型

基本类型：

| Proto Type | Notes                                                        | C++ Type | Java Type  |
| ---------- | ------------------------------------------------------------ | -------- | ---------- |
| double     | --                                                           | double   | double     |
| float      | --                                                           | float    | float      |
| int32      | 可变长编码，负数用sint32                                     | int32    | int        |
| int64      | 可变长编码，负数用sint64                                     | int64    | long       |
| uint32     | 可变长编码                                                   | uint32   | int        |
| uint64     | 可变长编码                                                   | uint64   | long       |
| sint32     | 可变长编码，有符号数，比int32高效                            | int32    | int        |
| sint64     | 可变长编码，有符号数，比int64高效                            | int64    | long       |
| fixed32    | Always four bytes.More efficient than uint32 if values are often greater than 2<sup>28</sup> | uint32   | int        |
| fixed64    | Always eight bytes.More efficient than uint64 if values are often greater than 2<sup>56</sup> | uint64   | long       |
| sfixed32   | Always four bytes                                            | int32    | int        |
| sfixed64   | Always eight bytes                                           | int64    | long       |
| bool       | --                                                           | bool     | boolean    |
| string     | A string must always contain UTF-8 encoded or 7-bit ASCII text, and cannot be longer than 2<sup>32</sup> | string   | String     |
| bytes      | May contain any arbitrary sequence of bytes no longer than 2<sup>32</sup> | string   | ByteString |

#### 2.2.3 字段默认值

`Proto2`支持声明默认值，`proto3`采用以下规则：

> 1. 对于`string`类型，默认值为一个空字符串；
> 2. 对于`bytes`类型，默认值为一个空的`byte`数组；
> 3. 对于`bool`类型，默认值为`false`；
> 4. 对于数值类型，默认值为`0`；
> 5. 对于枚举类型，默认值为第一项，也即值为`0`的那个枚举值；
> 6. 对于引用其它`message`类型：其默认值和对应的语言是相关的。

#### 2.2.4 `TAG`

* 对于同一个`message`里面的字段，每个字段的`Tag`是必须唯一数字；
* `Tag`主要用于说明字段在二进制文件的对应关系，一旦指定字段为对应的`Tag`，不应该在后续进行变更；
* 对于`Tag`的分配，`1~15`只用一个`byte`进行编码（因此应该留给那些常用的字段），`16~2047`用两个`byte`进行编码，最大支持到`536870911`，但是中间有一段（`19000~19999`）是`protobuf`内部使用的；
* 可以通过`reserved`关键字来预留`Tag`和字段名，还有一种场景时如果某个字段已经被废弃了，不希望后续被采用，也可以用`reserved`关键字声明。

#### 2.2.5 字段含义

```protobuf
syntax = "proto3";

package tutorial;

option java_package = "com.sty.ne.protocolbuffer";
option java_outer_classname = "AddressProto";

message Person {
  string name = 1;
  int32 id = 2;
  string email = 3;
  
  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }
  
  message PhoneNumber {
    string number = 1;
    PhoneType type = 2;
  }
  
  repeated PhoneNumber phones = 4;
}

message Address {
  repeated Person people = 1;
}
```

* **`syntax`**：`syntax="proto3"`指定`protocol buffer`版本，不写默认为`proto2`;
* **`option`**：`Option java_package = "xxx"`对应环境下的处理方式；
* **`repeated`**：`repeated Person people = 1`字段可以重复多次（不指定表示`0`次或`1`次）赋值，包括`0`次，重复的值的顺序会被保留，相当于动态变化的数组，设置`(list)[packed=true]`开启高效编码。

### 2.3 编译消息

#### 2.3.1 命令行编译

终端输入命令：

```bash
# SRC_DIR   编译的.proto文件目录
# --xxx_out 设置生成diam类型
# java:			--java_out  C++: --cpp_out
# DST_DIR   代码生成目录
# xxx.proto proto路径
protoc -I=$SRC_DIR -xxx_out =$DST_DIR $SRC_DIR/xxx.proto

# 在/Users/doc下生成对应的java文件
protoc -I=/Users/doc/Android/protobuffer/app/src/mainproto --java_out=/Users/doc /Users/doc/Android/protobuffer/app/src/main/proto/address.proto
```

#### 2.3.2 插件方式

`build`一下就好了，生成的文件路径如下：

![image](https://github.com/tianyalu/NeProtocolBuffer/raw/master/show/protocol_buffer_generate_path.png)

### 2.4 平台应用

* 命令行方式：需要手动拷贝代码到指定目录；

* 插件方式：不需要手动拷贝代码；

* 代码使用：

  ```java
  private void buildPerson() {
    AddressProto.Person.Builder builder = AddressProto.Person.newBuilder();
    AddressProto.Person person = builder.setEmail("xxx").build();
  }
  ```

* 在项目中使用序列化与反序列化：

  ```java
  // 序列化
  byte[] bytes = person.toByteArray();
  
  //反序列化
  try {
    AddressProto.Person personl = AddressProto.Person.parseFrom(bytes);
  } catch(InvalidProtocolBufferException e) {
    e.printStackTrace();
  }
  //序列化
  ByteArrayOutputStream output = new ByteArrayOutputStream();
  try {
    person.writeTo(output);
    byte[] bytes1 = output.toByteArray();
  } catch(IOException e) {
    e.printStackTrace();
  }
  //反序列化
  try {
    AddressProto.Person person1 = AddressProto.Person.parseFrom(new ByteArrayInputStream(bytes));
  } catch(IOException e) {
    e.printStackTrace();
  }
  ```

## 三、`Protocol Buffer`原理

* 编码机制：`Base 128 Varints`;
* 消息结构：`key-value`键值对组成；
* 使用指南：`Protocol Buffer`使用指南。

### 3.1 编码机制

`Varints`是一种可变字节序列化整形的方法：

* 每个`Byte`的最高位(`msb`)是标志位，如果该位为1，表示该`Byte`后面还有其它`Byte`，如果该位为0，表示该`Byte`是最后一个`Byte`；
* 每个`Byte`的低7位是用来存数值的位；
* `Varints`方法用`Litte-Endian`(小端)字节序。

> 一个多位整数按照其存储地址的最低位或最高字节进行排列，如果最低有效位在最高有效位的前面，则称小端序；反之则称为大端序。

**即：小端模式-->低位低地址（与阅读习惯相反）；大端模式-->高位低地址（与阅读习惯相反）**

如`0x1234567`的大端字节序和小端字节序的写法如下图所示：

![image](https://github.com/tianyalu/NeProtocolBuffer/raw/master/show/little_endian_mode.png)

### 3.2 消息结构

#### 3.2.1 编码类型（`wire_type`）

| Type | Meaning          | Used For                                                 |
| ---- | ---------------- | -------------------------------------------------------- |
| 0    | Varint           | int32, int64, uint32, uint64, sint32, sint64, bool, enum |
| 1    | 64-bit           | fixed64, sfixed64, double                                |
| 2    | Length-delimited | string, bytes, embedded messages, packed repeated fields |
| 3    | Start group      | groups (deprecated)                                      |
| 4    | End group        | groups (deprecated)                                      |
| 5    | 32-bit           | fixed32, sfixed32, float                                 |

#### 3.2.2 `key`

`key`的具体值为`(field_number << 3) | wire_type`；

`key`的范围：`wire_type`只有六中类型，用`3bit`表示，在一个`Byte`里，去掉`mbs`以及`3bit`的`wire_type`，只剩下`4bit`来表示`field_number`，因此，一个`Byte`里，`field_number`只能表达`0~15`，如果超过15，则需要两个或者多个`Byte`来表示。

#### 3.2.3 `Varint`优缺点

* `Varint`适用于表达比较小的整数，当数字很大时，采用定长编码类型（`64bit`、`32bit`）;
* `Varint`不利于表达负数，负数采用补码表示，会占用更多字节，因此如果确定会出现负数可采用`sint32`或者`sint64`，它会采用`ZigZig`编码将负数映射成整数，之后再使用`Varint`编码。

```java
message Test {
  int32 a = 1; //a = 150
}
//tag = 1
int32 varint wire_type = 0;
key = field_number<<3 | wire_type = 1<<3 | 0 = 1000 = 0x08
a = 150 = 1001 0110
因为要mbs,拆分： 1 0010110 --> 小端序 --> 0010110 1 --> +mbs --> 10010110 00000001 = 0x9601
最终编码 = key + value = 0x089601
```

#### 3.2.4 `Length-delimited`

`Length-delimited`编码格式则会将数据的`length`也编码进最终数据，使用`Length-delimited`编码格式的数据包括`string`，`bytes`和自定义消息。

```java
message Test2 {
  string b = 2; //b = "testing"
}
//tag = 2
string varint wire_type = 2
key = field_number<<3 | wire_type = 2<<3 | 2 = 10000 | 10 = 10010 = 0x12
len = 7 = 0x07                       
最终编码 = key + len + value = 0x12 07 74 65 73 74 69 6e 67  
//                                    t  e  s  t  i  n  g
```

#### 3.2.5 `repeated/packed`

`repeated`类型是把每个字段依次进行序列化，`key`相同，`value`不同，但如果`repeated`的字段较多，每次都带上相同的`key`会浪费空间，因此`protobuf`提供了`packed`选项，当`repeated`字段设置了`packed`选项，则会使用`Length-delimited`格式来编码字段值。

> `Proto2`需要指定`packed`参数，`proto3`数字类型默认开启该参数。

### 3.3 使用指南

* 尽量不要修改`tag`；
* 字段数量不要超过16个，否则会采用2字节编码；
* 如果确定使用负数，采用`sint32/sint64`。




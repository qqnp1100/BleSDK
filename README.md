# StwSDK配置
## 说明
SDK使用`rxjava`和`lambda语法`制作，请务必熟悉`rxjava`和`lambda语法`的使用。
## 环境配置
* 项目根目录下`build.gradle`
```groovy
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.3'
    }
```
* app下`build.gradle`
```groovy
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    allprojects {
        apply plugin: 'me.tatarka.retrolambda'
    }
    dependencies {
        compile 'com.alibaba:fastjson:1.1.56.android'
    }
```
* 添加2个jar包
    * [rxandroidble.jar](./app/libs/rxandroidble.jar)
    * [StwSDK.jar](./app/libs/StwSDK.jar)
* 添加`.so`文件  
将[jniLibs](./app/src/main/jniLibs)目录下文件拷贝到项目相同目录中

## 初始化SDK
* 在`Application`中初始化sdk
```java
StwSDK.init(this);
```
* 在合适的地方载入配置文件
```java
StwSDK.getInstance().setConfig();
```

## 使用
SDK未加入权限的判断和申请，请自行处理。anroid 6.0及以上需要gps权限。
结束activity时请务必解除所有订阅!
## 基本操作
### 扫描
```java
Subscription scanSub = StwSDK.getInstance().scan().subscribe(device -> {

 });//开始扫描

scanSub.unsubscribe();//取消扫描
```
### 连接
```java
StwSDK.getInstance().connect("AA:BB:CC:DD:EE:FF");//不自动重连
StwSDK.getInstance().connect("AA:BB:CC:DD:EE:FF",1000);//自动重连，第二个参数为超时时间
```
### 断开连接
```java
StwSDK.getInstance().disConnect();
```
### 蓝牙状态
```java
StwSDK.getInstance().bleState().subscribe(state -> {
            switch (state) {
                case CONNECTED:
                    break;
                case DISCONNECT:
                    break;
                case CONNECTTING:
                    break;
            }
        });
```
## 电子烟数据监听
电子烟状态改变时会发射`StwBean`,再通过switch获取`StwBean`内的数据。
```java
StwSDK.getInstance().bleData().subscribe(data -> {
            switch (data.getCommandType()) {
                case BATTERY:
                data.getBattery();
                    break;
                case POWER:
                    break;
                case VOLTAGE:
                    break;
                case TEMPERATURE:
                    break;
                case BYPASS:
                    break;
                case CUSTOM:
                    break;
                case TCR:
                    break;
                case VERSION:
                    break;
                case LINE_DATA:
                    break;
                case ATOMIZER:
                    break;
            }
        });
```
## 设置/查询(部分)
大部分api都加入了异常，当模式不支持或数值有误时进入异常。
### 设置功率
传入数值为实际功率10倍
```java
try 
{
    StwSDK.getInstance().setModePower(500);
}
catch (BleException e){}
```
### 设置电压
传入数值为实际电压100倍
```java
try 
{
    StwSDK.getInstance().setModeVoltage(500);
}
```
### 设置温度
```java
try 
{
    StwSDK.getInstance().setModeTemp(TemperatureUnit.Celsius, 300, Atomizer.Ni, true);
}
catch (BleException e){}
```
### 设置名称
18个字节以内
```java
StwSDK.getInstance().setName("STW");
```
### 查询工作模式
```java
StwSDK.getInstance().getWorkMode();
```
## 固件升级
### 开始升级
```java
StwSDK.getInstance().updata(binbytes,OnUpdataLisener);
```
### 取消升级
```java
StwSDK.getInstance().updataCancel();
```
### 升级回调 OnUpdataLisener
```java
new StwSDK.OnUpdataLisener() { 
                        @Override
                        public void onUpdata(int i) {
                            //升级进度 i 1~100
                        }

                        @Override
                        public void onSuccess() {
                            //升级成功
                        }

                        @Override
                        public void onFile(int i) {
                            //升级失败,错误码 i
                        }
                    }
```
## 感谢
* [Rxjava](https://github.com/ReactiveX/RxJava)
* [RxAndroidBle](https://github.com/Polidea/RxAndroidBle)
* [fastjson](https://github.com/alibaba/fastjson)
* [gradle-retrolambda](https://github.com/evant/gradle-retrolambda)
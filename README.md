# WifiConnHelper
Help connect wifi

## Usage

### Step 1
Add dependencies in build.gradle.

```groovy
    dependencies {
        compile 'com.gxw:wificonnhelperlib:1.0.2'
    }
```

### Step 2
It's very simple use just like .
```java
    WifiConnector build = new WifiConnector(this);
    ScanResult result = wifiBeens.get(postion).getScanResult();
    //connect wifi with SSID and password(if has passwprd)
    build.addWifiBeanConn(new WifiBeanConn("", result)).connectWithSSID(this);
    //connect wifi wif WifiConfiguration
    build.addWifiConfig(exsits).connectWithConfig(this);
```

Add Listener and do what you want in different states
```java
    @Override
    public void onWifiConnectStart(String ssid, String bssid) {
        LogUtil.showInfo(TAG, ssid + " connect is starting ");
    }

    @Override
    public void onWifiConnectSuccess(String ssid, String bssid) {
        LogUtil.showInfo(TAG, ssid + " connect success ");
    }

    @Override
    public void onWifiConnnectFail(String ssid, String bssid, int reason) {
        LogUtil.showInfo(TAG, ssid + " connect fail " + reason);
    }
```

## Contact me

If you have a better idea or way on this project, please let me know, thanks :)

[Email](mailto:603004002@qq.com)

[My Blog](http://blog.csdn.net/onepiece2)

### License
```
Copyright 2017 Bird Guo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
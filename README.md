# AvidAccountKitDemo
## AvidAccountSdk接入注意事项：
###1. 确保接入HolaAnalysisSDK，包括初始化、manifest中加入provider和receiver
###2. 工程minSdkVersion不得低于库文件avid_accountkit_****.aar的minSdkVersion
###3. gradle中拉取的accountkitsdk版本必须与内部版本号一致（当前为4.14.0）
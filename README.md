# MyoParrotMini


一个很酷的APP,可以让你通过 **MYO** 臂环远程控制 **Parrot Minidrone** 飞行器
----

## 前言
Myo 这款产品最早是一个朋友推荐给我的,买它是因为国外有极客玩家用它去控制 Parrot 飞行器

所以有一直有钢铁侠梦的我毫不犹豫的买了一个 **Myo** 臂环以及一台最便宜的 **Parrot Minidrone**

然而,小众产品的通病…**Myo官方用于控制 Parrot 的 APP 由于很久不更新,已经不支持现在的 Parrot Minidrone 设备**...所以,买回来还是装不了X

本人本着技术人装X到底的态度,决定自己动手,花了几天业余时间写一个 APP,实现了基本的控制功能,顺带修改一些功能.

由于 MYO Android SDK 很久没维护了,和 AS 以及 Parrot Android SDK出现兼容性问题,所以只能手动反编译 Myo 的 SDK 加以修改.

反编译后已解决兼容性问题的 Myo SDK 在项目里,有兴趣的同学可以拿去玩玩,但是没有发现开原协议...

时间仓促,水平有限,代码各位就凑合看吧.

## 预览

![img1](https://ww4.sinaimg.cn/large/006tNbRwjw1fb5c8yeonej30ci0godig.jpg)

![img2](https://ww4.sinaimg.cn/large/006tNbRwjw1fb5c8y9k15j30ir0p0wl7.jpg)

![img3](https://ww2.sinaimg.cn/large/006tNc79gw1fb5bagiybqg308c06yqv7.gif)

![img4](https://ww4.sinaimg.cn/large/006tNc79gw1fb5br2idcgg308c06ykjn.gif)

![img5](https://ww2.sinaimg.cn/large/006tNbRwjw1fb5c8y1zsyj30ir0p0tdl.jpg)

![img6](https://ww1.sinaimg.cn/large/006tNbRwgw1fb5cizpm2wj30p00e2myh.jpg)

## 使用手势
- 双击手指: 起飞/降落
- 手掌向外: 上升
- 手掌向内: 下降
- 手臂前倾: 前倾
- 手臂后仰: 后仰
- 手臂右旋: 右倾
- 手臂左旋: 左倾
- 握拳手臂右旋: 水平向右旋转
- 握拳手臂左旋: 水平向左旋转


## 感兴趣?

**Myo** 官网: https://www.myo.com

**Parrot** 官网: https://www.parrot.com/fr#minidrones

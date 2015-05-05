#课程宝更新日志

#####A android app developed to help college student management courses.

##2015/5/5
###第一次迭代还可以改善的地方：

######1.实现断网的情况也可以访问缓存的内容。<br>

######2.与服务器的交互中，处理一些边界的极端情况，减少崩溃的可能。<br>

######3.界面风格的统一，现在先统一使用蓝白的风格吧，范例在截图中有，复杂了今后再来改。<br>

######4.各个Activity的改善：<br>

    1.LoginActivity:
        1.缓存用户名和密码，自动登录
        2.添加背景，纯白看起来有点空洞
    2.ShowCourse
        1.添加Logout功能，直接调用finish()，会自动跳回到LoginActivity的界面
        2.判断今天是星期几，然后自动滑动今天的课程首页，更贴心点，还可以使用toast通知用户今天什么时候有课或者今天没课
        2.每个ListItem内的布局稍微调整一下，感觉很简陋，显示星期的字体可以设置的大一点或者居中
        3.不知道可不可以叫一个滑动的背景，每次滑动，然后背景移动一点点
    3.ActGroup
        1.替换一下“切换Acitivity”的按钮图片，不够形象，如果找不到，使用文字替代也可以
    4.Show Notice
        JC还没有实现Notice的功能，所以暂时还无法显示
    5.FAQActivity
        1.调整一下ListItem内的布局
        2.回答问题或者提出问题后，ListView能够自动更新
    6.ShowChapter
        1.调整一下ListItem内的布局


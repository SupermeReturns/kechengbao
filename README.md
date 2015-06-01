#课程宝更新日志

#####A android app developed to help college student management courses.

##2015/6/1 第三次迭代
### 更新（DONE 表示已解决）：
DONE 添加用于输入错误时候的提示 <br>
DONE 设计应用图标和名字 <br>
DONE ShowFAQ的ListView的长度和提问按钮重叠了，导致部分内容无法看到<br>
DONE 提问和回答的界面有一个返回箭头表示分界面的标识，并且背景改变为蓝色<br>
DONE 各个Title的文字(ShowCourse, ActivityAsk, ActivityAnswer)<br>
DONE FAQ Activity有时候没有显示网络故障，尽管已经连接上了网络<br>
DONE 将扫一扫和LOGOUT放在ActionBar的setting中，并且把ActionBar的背景改成Dark Blue<br>
DONE ShowCourse的ActionBar上面加上应用图标<br>
DONE ShowCourses的tileBar和按钮背景<br>
DONE 应用风格的统一：ActGroup，answer, ask等等的各部分调整为blue.dark, 然后看一下效果如何。<br>
DONE ActrionBar的popupmenu使用白色背景<br>
DONE ActGroup的按键问题：<br>
        1.无法知道那个按键被点中<br>
        2.按键点击感不够<br>
DONE 按键没有点击感<br>
DONE ActivityGroup图标大小减小<br>
DONE ShowNotice的布局<br>
DONE 实现ActGroup到Ask和Answer之后返回更新的功能<br>

###计划：
各个ListView中的排序<br>
ActionBar Left caret的R.id是多少？<br>
给课程宝取个更好的名字吧~<br>
显示一个课程的所有其他信息<br>
ShowNotice实现一段时间之后不停进行查询<br>
AnswerActivity的布局问题<br>
ShowFAQ的用户名高亮功能<br>
提示用户的密码错误<br>
点赞/同问功能<br>
作业提醒功能<br>


##2015/5/23 第二次迭代
### 更新：
1.实现离线功能<br>
2.二维码扫描(未测试)<br>
3.修改了离线登录条件下无法自动登录的BUG<br>
4.修改了离线加载等待时间太长的BUG<br>
5.屏蔽掉ShowCource界面的返回按钮默认功能,变成home键功能<br>
3.界面优化：<br>
    
 1. 更新界面背景
 2. ListView中的文字格式

###计划：
1.提问和回答的界面有一个返回箭头表示分界面的标识，并且背景改变为蓝色<br>
2.按键没有点击感<br>
3.ActGroup的按键问题：<br>
    
 1. 无法知道那个按键被点中
 2. 按键点击感不够


##2015/5/5 第一次迭代
###还可以改善的地方：

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



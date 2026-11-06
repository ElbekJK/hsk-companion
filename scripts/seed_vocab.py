#!/usr/bin/env python3
"""Create a small self-contained starter pack; full lists are fetched on Debian."""
import json
from pathlib import Path

# These are independently written beginner glosses, not copied from a dictionary.
# The seed is intentionally a teaching subset, NOT a complete official word list.
HSK1 = '''
爱|ài|to love
八|bā|eight
爸爸|bàba|dad
杯子|bēizi|cup or glass
北京|Běijīng|Beijing
本|běn|measure word for books
不|bù|not
不客气|bú kèqi|you're welcome
菜|cài|dish or vegetable
茶|chá|tea
吃|chī|to eat
出租车|chūzūchē|taxi
打电话|dǎ diànhuà|to make a phone call
大|dà|big
的|de|possessive particle
点|diǎn|o'clock or point
电脑|diànnǎo|computer
电视|diànshì|television
电影|diànyǐng|movie
东西|dōngxi|thing
都|dōu|all
读|dú|to read
对不起|duìbuqǐ|sorry
多|duō|many or much
多少|duōshao|how much or how many
儿子|érzi|son
二|èr|two
饭店|fàndiàn|restaurant or hotel
飞机|fēijī|airplane
分钟|fēnzhōng|minute
高兴|gāoxìng|happy
个|gè|general measure word
工作|gōngzuò|work or job
狗|gǒu|dog
汉语|Hànyǔ|Chinese language
好|hǎo|good
号|hào|number or date
喝|hē|to drink
和|hé|and
很|hěn|very
回|huí|to return
会|huì|can or to know how
几|jǐ|how many
家|jiā|home or family
叫|jiào|to be called
今天|jīntiān|today
九|jiǔ|nine
开|kāi|to open
看|kàn|to look or watch
看见|kànjiàn|to see
块|kuài|yuan or piece
来|lái|to come
老师|lǎoshī|teacher
了|le|completed-action particle
冷|lěng|cold
里|lǐ|inside
六|liù|six
妈妈|māma|mom
吗|ma|question particle
买|mǎi|to buy
猫|māo|cat
没|méi|not have or did not
没关系|méi guānxi|it doesn't matter
米饭|mǐfàn|cooked rice
明天|míngtiān|tomorrow
名字|míngzi|name
哪|nǎ|which
那|nà|that
呢|ne|follow-up question particle
能|néng|to be able to
你|nǐ|you
年|nián|year
女儿|nǚ'ér|daughter
朋友|péngyou|friend
漂亮|piàoliang|pretty
苹果|píngguǒ|apple
七|qī|seven
钱|qián|money
前面|qiánmiàn|in front
请|qǐng|please
去|qù|to go
热|rè|hot
人|rén|person
认识|rènshi|to know someone
三|sān|three
商店|shāngdiàn|shop
上|shàng|above or on
上午|shàngwǔ|morning
少|shǎo|few or little
谁|shéi|who
什么|shénme|what
十|shí|ten
时候|shíhou|time or moment
是|shì|to be
书|shū|book
水|shuǐ|water
水果|shuǐguǒ|fruit
睡觉|shuìjiào|to sleep
说|shuō|to speak
四|sì|four
岁|suì|years old
他|tā|he
她|tā|she
太|tài|too or extremely
天气|tiānqì|weather
听|tīng|to listen
同学|tóngxué|classmate
喂|wèi|hello (on the phone)
我|wǒ|I or me
我们|wǒmen|we
五|wǔ|five
喜欢|xǐhuan|to like
下|xià|below or next
下午|xiàwǔ|afternoon
下雨|xiàyǔ|to rain
先生|xiānsheng|Mr. or sir
现在|xiànzài|now
想|xiǎng|to want or think
小|xiǎo|small
小姐|xiǎojiě|Miss
些|xiē|some
写|xiě|to write
谢谢|xièxie|thank you
星期|xīngqī|week
学生|xuésheng|student
学习|xuéxí|to study
学校|xuéxiào|school
一|yī|one
衣服|yīfu|clothing
医生|yīshēng|doctor
医院|yīyuàn|hospital
椅子|yǐzi|chair
有|yǒu|to have
月|yuè|month
再见|zàijiàn|goodbye
在|zài|at or in
怎么|zěnme|how
怎么样|zěnmeyàng|how is it
这|zhè|this
中国|Zhōngguó|China
中午|zhōngwǔ|noon
住|zhù|to live or reside
桌子|zhuōzi|table
字|zì|character
昨天|zuótiān|yesterday
坐|zuò|to sit or ride
做|zuò|to do or make
'''
HSK2 = '''
吧|ba|suggestion particle
白|bái|white
百|bǎi|hundred
帮助|bāngzhù|to help
报纸|bàozhǐ|newspaper
比|bǐ|than or to compare
别|bié|don't
宾馆|bīnguǎn|hotel
长|cháng|long
唱歌|chànggē|to sing
出|chū|to go out
穿|chuān|to wear
次|cì|occasion or time
从|cóng|from
错|cuò|wrong
打篮球|dǎ lánqiú|to play basketball
大家|dàjiā|everyone
到|dào|to arrive
得|de|complement particle
等|děng|to wait
弟弟|dìdi|younger brother
第一|dì yī|first
懂|dǒng|to understand
对|duì|correct or toward
房间|fángjiān|room
非常|fēicháng|extremely
服务员|fúwùyuán|waiter or attendant
高|gāo|tall or high
告诉|gàosu|to tell
哥哥|gēge|older brother
给|gěi|to give
公共汽车|gōnggòng qìchē|public bus
公司|gōngsī|company
贵|guì|expensive
过|guo|experience particle
孩子|háizi|child
还|hái|still or also
黑|hēi|black
红|hóng|red
欢迎|huānyíng|welcome
回答|huídá|to answer
机场|jīchǎng|airport
鸡蛋|jīdàn|egg
件|jiàn|measure word for clothing
教室|jiàoshì|classroom
姐姐|jiějie|older sister
介绍|jièshào|to introduce
进|jìn|to enter
近|jìn|near
就|jiù|then or right away
觉得|juéde|to feel or think
咖啡|kāfēi|coffee
开始|kāishǐ|to begin
考试|kǎoshì|exam
可能|kěnéng|possible or perhaps
可以|kěyǐ|may or can
课|kè|class or lesson
快|kuài|fast
快乐|kuàilè|happy or joyful
累|lèi|tired
离|lí|away from
两|liǎng|two (quantity)
零|líng|zero
路|lù|road
旅游|lǚyóu|to travel
卖|mài|to sell
慢|màn|slow
忙|máng|busy
每|měi|every
妹妹|mèimei|younger sister
门|mén|door
面条|miàntiáo|noodles
男|nán|male
您|nín|you (polite)
牛奶|niúnǎi|milk
旁边|pángbiān|beside
跑步|pǎobù|to run or jog
便宜|piányi|cheap
票|piào|ticket
妻子|qīzi|wife
起床|qǐchuáng|to get up
千|qiān|thousand
铅笔|qiānbǐ|pencil
晴|qíng|sunny
去年|qùnián|last year
让|ràng|to let or allow
日|rì|day
上班|shàngbān|to go to work
身体|shēntǐ|body or health
生病|shēngbìng|to get sick
生日|shēngrì|birthday
时间|shíjiān|time
事情|shìqing|matter or thing
手表|shǒubiǎo|wristwatch
手机|shǒujī|mobile phone
送|sòng|to give or send
所以|suǒyǐ|therefore
它|tā|it
踢足球|tī zúqiú|to play football
题|tí|question or problem
跳舞|tiàowǔ|to dance
外|wài|outside
完|wán|to finish
玩|wán|to play
晚上|wǎnshang|evening
往|wǎng|toward
为什么|wèishénme|why
问|wèn|to ask
问题|wèntí|question or problem
西瓜|xīguā|watermelon
希望|xīwàng|to hope
洗|xǐ|to wash
小时|xiǎoshí|hour
笑|xiào|to laugh or smile
新|xīn|new
姓|xìng|surname
休息|xiūxi|to rest
雪|xuě|snow
颜色|yánsè|color
眼睛|yǎnjing|eyes
羊肉|yángròu|mutton
药|yào|medicine
要|yào|to want or need
也|yě|also
已经|yǐjīng|already
一起|yìqǐ|together
意思|yìsi|meaning
因为|yīnwèi|because
阴|yīn|cloudy
游泳|yóuyǒng|to swim
右边|yòubiān|right side
鱼|yú|fish
远|yuǎn|far
运动|yùndòng|exercise or sport
再|zài|again
早上|zǎoshang|early morning
丈夫|zhàngfu|husband
找|zhǎo|to look for
着|zhe|ongoing-state particle
真|zhēn|really
正在|zhèngzài|currently doing
知道|zhīdào|to know a fact
准备|zhǔnbèi|to prepare
走|zǒu|to walk
最|zuì|most
左边|zuǒbiān|left side
'''

def parse(data, level):
    words = []
    for line in data.strip().splitlines():
        a, b, c = (part.strip() for part in line.split('|', 2))
        words.append({'version': '2.0', 'level': level, 'hanzi': a, 'pinyin': b, 'english': c})
    return words

words = parse(HSK1, 1) + parse(HSK2, 2)
out = Path(__file__).resolve().parents[1] / 'app/src/main/assets/vocabulary.json'
out.write_text(json.dumps(words, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Wrote starter pack with {len(words)} words (not a complete official HSK list).')

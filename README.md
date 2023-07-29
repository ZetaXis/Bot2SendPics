# Bot2SendPics
A simple bot for Telegram to send multiple pictures across channels.

Bot.properties description:

Bot.Username = NAMEBOT (without @)

Bot.Token = Token when you're create your bot

Bot.LoggingConnections = true\false (info for register channels\local chat ID, adds some INFO

Bot.DefaultPeriod = 60 (minutes, regular sending)

Bot.AdminChatID = 123456789 (Chat ID of your local messages with that bot, via LoggingConnections you can see that ID)

Bot.countThreads = 4 (Thread counts, for support more than 1 channel)

How to use:

1) register your bot via https://t.me/BotFather
2) write Token and Username from BotFather data in bot.properties
3) also check LoggingConnections, DefaultPeriod, countThreads
4) start java server for bot
5) add your bot to your channels, write something in channels, he is create folder for your sending data in /db/IDYOURCHANNEL
6) add material, and... that's all, i think ;)

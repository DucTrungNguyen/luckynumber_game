// author nguyentrung

var TIME_LIMIT = 15; // 15s truoc khi chon so ngau nhien
var SLEEP_TIME = 5; // 5 giay truoc khi gui

function User(id, money, betValue)
{
    this.id = id;
    this.money = money;
    this.betValue = betValue;
    // this.bird = bird
}

var ArrayList = require('arraylist')
var app = require('express')();
var http = require('http').Server(app)
var io = require('socket.io')(http)

//Tao  1 danh sach user dang choi
var listUser = new ArrayList;
var money = 0; // Tong tien

// Tao server
app.get('/', function(req, res){
    res.sendFile('index.html', {root: __dirname})

});

function getRandomInt(max)
{
    return Math.floor(Math.random()*Math.floor(max));
}

function sleep(sec)
{
    return new Promise(resolve => setTimeout(resolve, sec*1000));
}

async function countDonw()
{
    var timeTotal = TIME_LIMIT;
    do
    {
        // send time to all client
        io.sockets.emit('broadcast', timeTotal)
        timeTotal--;
        await sleep(1)

    }while(timeTotal > 0)

    //after finish
    processResult(); // gui giai thuong

    timeTotal = TIME_LIMIT;
    timeSleep =  SLEEP_TIME;
    money = 0;
    do {
        io.sockets.emit('wait_before_restart', timeSleep)
        timeSleep--;
        await sleep(1)
    }while( timeSleep > 0)
    io.sockets.emit('money_send', 0);

    
    io.sockets.emit('restart', 1) // gui tin nhan cho tat ca user

    countDonw();
}

function processResult()
{
    console.log('server is pricessing data')
    var result = getRandomInt(10)
    console.log('asdas', "Lucky Number: " + result);
    io.sockets.emit('result', result)

    listUser.unique()

    // for (u in listUser){
    //     console.log(u.betValue)
    // }
    // dem so ng choi thang
    var count = listUser.find(function(user){
        return user.betValue == result;
    }).length;

    //dem so ng thang  hay thua de gui thuong
    listUser.find(function (user){
        if ( user.betValue == result){
            io.to(user.id).emit('reward', parseInt(user.money)*2);
            console.log( "win: " + user.betValue)
        }
            
        else{
            io.to(user.id).emit('lose', user.money); 
            console.log( "win: " + user.betValue)

        }
            
    
    });

    console.log('We have ' + count + ' people win');
    listUser.clear();
}

// xu ly ket noi socket
io.on('connection', function (socket){

    console.log('A new user '+ socket.id + ' is connected')
    io.sockets.emit('monney_Send', money)
    socket.on('client_Send_money', function(objectClient){
        console.log(objectClient)
        var user = new User(socket.id, objectClient.money, objectClient.betValue);
        console.log('Wa receive :' + user.money + ' from ' + user.id)
        console.log('User: ' + user.id + ' bet value '+ user.betValue)
        money  += parseInt(user.money)

        console.log('=x1b[43', 'sum of money: ' + money)
        listUser.add(user)
        console.log('Total online user '+  listUser.length)

        io.sockets.emit('money_send ', money)

    });
    socket.on('disconnect', function(){

        console.log('User ' + socket.id + ' is leave')
    
    })
});

// When ever someonr disconect


http.listen(3000, function(){
    console.log('SERVER GAME STARTED ON PORT 3000')
    countDonw();
})
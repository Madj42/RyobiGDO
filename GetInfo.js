        var request = require('request');
// Change these variables
        var email = 'youremailaddress'
        var pass = 'yourpassword'

    const doSomething = () => new Promise((resolve, reject) => {
         var requestmsg = '{"username":"emailhere","password":"passwordhere"}'
        var requestmsg = requestmsg.replace('emailhere', email)
        var requestmsg = requestmsg.replace('passwordhere', pass)
        var requestmsg = JSON.parse(requestmsg)
var options = {url:'https://tti.tiwiconnect.com/api/login',method:'POST',json:requestmsg}
        request(options, (err, res, body) => {
        if (err) return reject(err)
        resolve(body)
    })
})

const someController = async function() {
    var someValue = await doSomething()
                console.log(someValue)
}

    const doSomething2 = () => new Promise((resolve, reject) => {
         var requestmsg = '{"username":"emailhere","password":"passwordhere"}'
        var requestmsg = requestmsg.replace('emailhere', email)
        var requestmsg = requestmsg.replace('passwordhere', pass)
        var requestmsg = JSON.parse(requestmsg)
var options = {url:'https://tti.tiwiconnect.com/api/devices',method:'GET',json:requestmsg}
        request(options, (err, res, body) => {
        if (err) return reject(err)
        resolve(body)
    })
})

const someController2 = async function() {
    var someValue = await doSomething2()
                console.log(someValue)
}

someController()
someController2()

'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  genNewUser,
  genNewUserReply,
  genNewAuction,
  genNewAuctionReply,
  genNewQuestion,
  genNewQuestionReply
}


const Faker = require('faker')
const fs = require('fs')
var users = []
var auctions = []
var questions = []
// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
var statsPrefix = [["/rest/user/","GET"],
                   ["/rest/user/","POST"],
                   ["/rest/auction/","POST"],
				   ["/rest/question/","POST"],]


// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
	var i = 0;
	for( i = 0; i < statsPrefix.length; i++) {
		if( str.startsWith( statsPrefix[i][0]) && method == statsPrefix[i][1])
			return method + ":" + statsPrefix[i][0];
	}
	return method + ":" + str;
}

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

/**
 * Select an image to download.
 */
function selectUser(context, events, done) {
	if( userIds.length > 0) {
		context.vars.userId = userIds.sample()
	} else {
		delete context.vars.userId
	}
	return done()
}

/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}
/**
 * Generate data for a new bid using Faker
 */
 function genNewQuestion(context, events, done) {
	context.vars.idQuestion = Faker.datatype.number({ min: 1000, max: 9999}).toString();
	context.vars.question = Faker.lorem.sentence(3) + "?"
	return done()
}
/**
 * Generate data for a new auctions using Faker
 */
 function genNewAuction(context, events, done) {
	context.vars.idAuction = Faker.datatype.number({ min: 1000, max: 9999}).toString();
	context.vars.title = Faker.lorem.sentence(3)
    context.vars.description = Faker.lorem.sentence(5)
    context.vars.minimumBid = Faker.datatype.number({ min: 10, max: 100})

	return done()
}

/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let body = response.body.replace(/(\r\n|\n|\r)/gm, "")
		let u = JSON.parse( body )
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

/**
 * Process reply for of new auctions to store the id on file
 */
function genNewAuctionReply(requestParams, response, context, ee, next) {
	console.log(response.body)
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let body = response.body.replace(/(\r\n|\n|\r)/gm, "")
		let u = JSON.parse( body )
		auctions.push(u)
		fs.writeFileSync('auctions.data', JSON.stringify(auctions));
	}
    return next()
}

/**
 * Process reply for of new users to store the id on file
 */
 function genNewQuestionReply(requestParams, response, context, ee, next) {
	console.log(response.body)
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let body = response.body.replace(/(\r\n|\n|\r)/gm, "")
		let u = JSON.parse( body )
		questions.push(u)
		fs.writeFileSync('bids.data', JSON.stringify(questions));
	}
    return next()
}


function authReply(requestParams, response, context, ee, next) {
    if( response.statusCode >= 200 && response.statusCode < 300)  {
        var cookie = response.rawHeaders[5].split(";")[0].split("=")[1]+ ";"
        context.vars.mycookie = cookie
    }   
    return next()
}    


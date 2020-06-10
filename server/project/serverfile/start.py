from flask import Flask, render_template
from flask_restful import Api, Resource, reqparse
from getImage import GetImage
from ocrapi import ocrRequest
from registerAccount import registerAccount
from login import LoginAccount
from friends_list import FriendList
from friends_request import FriendRequest
from content_list import ContentList

app = Flask(__name__)
api = Api(app)

@app.route('/')
def hello():
    return render_template('/index.html')

api.add_resource(GetImage,'/getImage')
api.add_resource(ocrRequest,'/ocr')
api.add_resource(registerAccount,'/register')
api.add_resource(LoginAccount,'/login')
api.add_resource(FriendList,'/friend_list')
api.add_resource(FriendRequest,'/friend_request')
api.add_resource(ContentList,'/content_list')

if __name__=='__main__':
    app.debug = True
    app.run()


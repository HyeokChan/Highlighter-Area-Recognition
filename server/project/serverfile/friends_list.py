from flask_restful import Resource, reqparse
from module import db

class FriendList(Resource):
    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('uuid',type=str)
            parser.add_argument('list_type',type=str)
            args= parser.parse_args()

            user_uuid = args['uuid']
            list_type = args['list_type']

            dbc = db.Database()

            if list_type == "total":
                sql = 'select * from user_friends where me_uuid=%s'
                row = dbc.executeAll(sql,user_uuid)
                if row:
                    result_row = []
                    for list in row:
                        temp = {}
                        temp['friend_email'] = list['friend_email']
                        result_row.append(temp)
                    return {"result":"success","friendList":result_row}

                else:
                    return{"result":"success","friendList":"not exist"}

            elif list_type == "send":
                sql = 'select * from user_friends_temp where me_uuid_temp=%s and friend_type=%s'
                row = dbc.executeAll(sql,(user_uuid,"send"))
                if row:
                    result_row = []
                    for list in row:
                        temp = {}
                        temp['friend_email'] = list['friend_email_temp']
                        result_row.append(temp)
                    
                    return {"result":"success","friendList":result_row}

                else:
                    return{"result":"success","friendList":"not exist"}

            elif list_type == "recv":
                sql = 'select * from user_friends_temp where me_uuid_temp=%s and friend_type=%s'
                row = dbc.executeAll(sql,(user_uuid,"recv"))
                if row:
                    result_row = []
                    for list in row:
                        temp = {}
                        temp['friend_email'] = list['friend_email_temp']
                        result_row.append(temp)
                    
                    return {"result":"success","friendList":result_row}

                else:
                    return{"result":"success","friendList":"not exist"}

        
        except Exception as e:
            return {'error':str(e)}

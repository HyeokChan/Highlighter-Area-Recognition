from flask_restful import Resource, reqparse
from module import db

class FriendRequest(Resource):
    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('uuid',type=str)
            parser.add_argument('me_email',type=str)
            parser.add_argument('another_email',type=str)
            parser.add_argument('type',type=str)
            args= parser.parse_args()

            user_uuid = args['uuid']
            me_email = args['me_email']
            send_email = args['another_email']
            request_type = args['type']
            dbc = db.Database()

            if request_type == "send":
                sql = 'select * from user_table where email=%s'
                row = dbc.executeAll(sql,send_email)

                if row:
                    another_uuid = row[0]['android_id']
                    sql = 'select * from user_friends where me_uuid=%s and friend_uuid=%s'
                    row = dbc.executeAll(sql,(user_uuid,another_uuid))
                    if row:
                        return {"result":"fail", "info":"already friends"}
                    else:
                        sql = 'insert into user_friends_temp(knowString, me_uuid_temp,friend_uuid_temp,friend_type,friend_email_temp) values(%s,%s,%s,%s,%s)'
                        dbc.execute(sql,((user_uuid+another_uuid),user_uuid,another_uuid,"send",send_email))
                        dbc.execute(sql,((user_uuid+another_uuid),another_uuid,user_uuid,"recv",me_email))
                        return {"result":"success"}
                else:
                    return {"error":"not exist email"}

            elif request_type == "recv":
                sql = 'select * from user_friends_temp where friend_uuid_temp=%s and friend_type=%s'
                row = dbc.executeAll(sql,(user_uuid,"send"))

                if row:
                    column = row[0]['knowString']
                    uuidA = row[0]['me_uuid_temp']
                    uuidB = row[0]['friend_uuid_temp']
                    mailA = send_email
                    mailB = row[0]['friend_email_temp']

                    sql = 'insert into user_friends(me_uuid,friend_uuid,friend_email) values(%s,%s,%s)'
                    dbc.execute(sql,(uuidA,uuidB,mailB))
                    dbc.execute(sql,(uuidB,uuidA,mailA))

                    sql = 'delete from user_friends_temp where knowString=%s'
                    dbc.execute(sql,column)

                    return {"result":"success"}
                else:
                    return {"result":"fail","info":"dont have that request"}
            
            elif request_type == "send_cancel":
                sql = 'select * from user_friends_temp where me_uuid_temp=%s and friend_email_temp=%s'
                row = dbc.executeAll(sql,(user_uuid,send_email))

                if row:
                    column = row[0]['knowString']
                    sql = 'delete from user_friends_temp where knowString=%s'
                    dbc.execute(sql,column)

                    return {'result':'success'}
                else:
                    return {"result":"fail","info":"not exist"}
            else:
                return {"result":"fail","info":"wrong type"}

        
        except Exception as e:
            return {'error':str(e)}
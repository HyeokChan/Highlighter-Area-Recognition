from flask_restful import Resource, reqparse
from module import db
import bcrypt

class LoginAccount(Resource):
    def checkid(self,dbc,email):
        try:
            sql = 'select * from user_table where email=%s'
            row = dbc.executeAll(sql,email)
            if row:
                return True
            else:
                return False
        except Exception as e :
            return {'error':str(e)}
    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('password',type=str)
            parser.add_argument('email',type=str)
            args= parser.parse_args()

            if args['password'] is None or args['email'] is None:
                return{'result':'fail','info':'not enough information'}

            user_pw = args['password']
            user_email = args['email']

            dbc = db.Database()

            user_pw = user_pw.encode('utf-8')
            check = self.checkid(dbc,user_email)
            if check:         
                sql = 'select * from user_table where email=%s'
                row = dbc.executeAll(sql,user_email)
                check_password = bcrypt.checkpw(user_pw,row[0]['password'].encode('utf-8'))
                if check_password == True:
                    return {'RESPONSE':'200 OK','result':'success'}
                else:
                    return {'result':'fail','info':'incorrect password'}
            else:
                return {'result':'fail','info':'not accounted email'}
        
        except Exception as e:
            return {'error':str(e)}

from flask_restful import Resource, reqparse
from module import db
import bcrypt

class registerAccount(Resource):
    def checkid(self,dbc,email):
        try:
            sql = 'select * from user_table where email=%s'
            row = dbc.executeAll(sql,email)
            if row:
                return {'result':'fail', 'info':'already exist email'}
        except Exception as e :
            return {'error':str(e)}

    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('password',type=str)
            parser.add_argument('email',type=str)
            parser.add_argument('uuid',type=str)
            args= parser.parse_args()

            user_pw = args['password']
            user_email = args['email']
            user_uuid = args['uuid']

            dbc = db.Database()
            check = self.checkid(dbc,user_email)
            if check:
                return check

            user_pw = (bcrypt.hashpw(user_pw.encode('utf-8'),bcrypt.gensalt())).decode('utf-8')
            if user_pw is None or user_email is None or user_uuid is None:
                return{'result':'fail','info':'not enough information'}
            
            else:
                sql = 'insert into user_table(email,password,android_id,imgindex) values(%s,%s,%s,%s)'
                dbc.execute(sql,(user_email,user_pw,user_uuid,"1"))
                return {'RESPONSE':'200 OK','result':'success'}
        
        except Exception as e:
            return {'error':str(e)}

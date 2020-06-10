from flask_restful import Resource, reqparse
from module import db
import base64

class ContentList(Resource):
    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('uuid',type=str)
            args= parser.parse_args()

            user_uuid = args['uuid']

            dbc = db.Database()

            sql = 'select * from ocr_img_table where a_id=%s '
            row = dbc.executeAll(sql,user_uuid)
            if row:
                result_row = []
                for list in row:
                    temp = {}
                    temp['doc_name'] = list['doc_name']
                    temp['doc_content'] = list['doc_content']
                    temp['image'] = base64.encodebytes(list['image']).decode("utf-8")
                    result_row.append(temp)
                return {"result":"success","my_content":result_row}
        
        except Exception as e:
            return {'error':str(e)}

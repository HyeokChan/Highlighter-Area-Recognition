from flask_restful import Resource, reqparse
import json, ast, base64, os
from module import callocrApi, recognize_box, db

class ocrRequest(Resource):
    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('filename',type=str)
            parser.add_argument('box',type=str)
            parser.add_argument('uuid',type=str)
            args= parser.parse_args()

            filename = args['filename']
            box = args['box']
            uuid = args['uuid']

            image_path = './upload/'+ filename
            if os.path.isfile(image_path) == False:
                return {'result':'fail', 'info':'file is not exist. first, call /getImage'}
            box_list = ast.literal_eval(box)

            recog = recognize_box.RecogArea(image_path)
            recog.newImage(box_list)

            ocrApi = callocrApi.OCRAPI()
            output = ocrApi.kakao_ocr_detect(recog.image_path).json()
            boxes = output["result"]["boxes"]
            
            output = ocrApi.kakao_ocr_recognize(recog.image_path, boxes).json()
            output = output['result']['recognition_words']
            output = format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False))


            with open(image_path,'rb')as f:
                image = f.read()
            image_encode = base64.b64encode(image)

            dbc = db.Database()
            sql = 'select * from user_table where android_id = %s'
            row = dbc.executeAll(sql,uuid)

            img_index = row[0]['imgindex']
            filename = filename[:-12]+"_"+str(img_index)+".jpg"

            sql = 'insert into ocr_img_table(a_id,doc_name,doc_content,image) values(%s,%s,%s,%s)'
            dbc.execute(sql,(uuid,filename,output,image_encode))

            sql = 'update user_table set imgindex=%s where android_id=%s'
            dbc.execute(sql,(img_index+1,uuid))
            if os.path.isfile(recog.image_path) == True:
                os.remove(recog.image_path)
                if os.path.isfile(recog.image_path[:-10]+"_resized.jpg") == True:
                    os.remove(recog.image_path[:-10]+"_resized.jpg")
                    if os.path.isfile(recog.image_path[:-10]+".jpg") == True:
                        os.remove(recog.image_path[:-10]+".jpg")

            return {'RESPONSE':'200 OK','result':'success','output':output}
        
        except Exception as e:
            return {'error':str(e)}
